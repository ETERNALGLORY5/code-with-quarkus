package org.acme.resource;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.model.Employee;

import java.util.List;

@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmployeeResource {

    @GET
    public Uni<List<Employee>> list() {
        return Employee.listAll();
    }

    @GET
    @Path("/{id}")
    public Uni<Employee> get(@PathParam("id") Long id) {
        return Employee.<Employee>findById(id)
                .onItem().ifNull().failWith(new NotFoundException("Employee not found"));
    }

    @POST
    public Uni<Response> create(@Valid Employee employee) {
        return Panache.withTransaction(employee::persist)
                .replaceWith(Response.status(Response.Status.CREATED).entity(employee).build());
    }

    @DELETE
    @Path("/{id}")
    public Uni<Response> delete(@PathParam("id") Long id) {
        return Panache.withTransaction(() -> Employee.deleteById(id))
                .map(deleted -> deleted
                        ? Response.noContent().build()
                        : Response.status(Response.Status.NOT_FOUND).build());
    }
}
