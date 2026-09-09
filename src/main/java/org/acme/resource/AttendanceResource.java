package org.acme.resource;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.model.AttendanceRecord;
import org.acme.model.AttendanceStatus;
import org.acme.model.Employee;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Path("/attendance")
@Produces(MediaType.APPLICATION_JSON)
public class AttendanceResource {

    @POST
    @Path("/checkin/{employeeId}")
    public Uni<Response> checkIn(@PathParam("employeeId") Long employeeId) {
        LocalDate today = LocalDate.now();
        return Panache.withTransaction(() ->
                Employee.<Employee>findById(employeeId)
                        .onItem().ifNull().failWith(new NotFoundException("Employee not found"))
                        .flatMap(employee -> AttendanceRecord
                                .<AttendanceRecord>find("employee = ?1 and attendanceDate = ?2", employee, today)
                                .firstResult()
                                .flatMap(existing -> {
                                    if (existing != null) {
                                        return Uni.createFrom().failure(new WebApplicationException(
                                                "Already checked in today", Response.Status.CONFLICT));
                                    }
                                    AttendanceRecord record = new AttendanceRecord();
                                    record.employee = employee;
                                    record.attendanceDate = today;
                                    record.checkInTime = LocalDateTime.now();
                                    record.status = AttendanceStatus.PRESENT;
                                    return record.persist().replaceWith(record);
                                }))
        ).map(record -> Response.status(Response.Status.CREATED).entity(record).build());
    }

    @POST
    @Path("/checkout/{employeeId}")
    public Uni<Response> checkOut(@PathParam("employeeId") Long employeeId) {
        LocalDate today = LocalDate.now();
        return Panache.withTransaction(() ->
                AttendanceRecord
                        .<AttendanceRecord>find("employee.id = ?1 and attendanceDate = ?2", employeeId, today)
                        .firstResult()
                        .onItem().ifNull().failWith(new NotFoundException("No check-in found for today"))
                        .map(record -> {
                            record.checkOutTime = LocalDateTime.now();
                            return record;
                        })
        ).map(record -> Response.ok(record).build());
    }

    @GET
    @Path("/employee/{employeeId}")
    public Uni<List<AttendanceRecord>> history(@PathParam("employeeId") Long employeeId) {
        return AttendanceRecord.list("employee.id", employeeId);
    }

    @GET
    public Uni<List<AttendanceRecord>> byDate(@QueryParam("date") String date) {
        LocalDate target = date != null ? LocalDate.parse(date) : LocalDate.now();
        return AttendanceRecord.list("attendanceDate", target);
    }
}
