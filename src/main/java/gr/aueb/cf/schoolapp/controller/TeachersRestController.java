package gr.aueb.cf.schoolapp.controller;

import gr.aueb.cf.schoolapp.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.schoolapp.core.exceptions.EntityInvalidArgumentException;
import gr.aueb.cf.schoolapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.schoolapp.dto.TeacherFiltersDTO;
import gr.aueb.cf.schoolapp.dto.TeacherInsertDTO;
import gr.aueb.cf.schoolapp.dto.TeacherReadOnlyDTO;
import gr.aueb.cf.schoolapp.dto.TeacherUpdateDTO;
import gr.aueb.cf.schoolapp.mapper.Mapper;
import gr.aueb.cf.schoolapp.service.ITeacherService;
import gr.aueb.cf.schoolapp.validator.ValidatorUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;



@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @__(@Inject))
@Path("/teachers")
public class TeachersRestController {
    private final ITeacherService teacherService;

    @GET
    @Path("/{teacherId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeacher(@PathParam("teacherId") Long id) throws EntityNotFoundException {
        TeacherReadOnlyDTO readOnlyDTO = teacherService.getTeacherById(id);
        return Response.status(Response.Status.OK).entity(readOnlyDTO).build();
    }


    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTeacher(TeacherInsertDTO dto, @Context UriInfo uriInfo) throws EntityInvalidArgumentException, EntityAlreadyExistsException {

        List<String> errors = ValidatorUtil.validateDTO(dto);
        if (!errors.isEmpty()) {
            throw new EntityInvalidArgumentException("Teacher", String.join(",", errors));
        }
        TeacherReadOnlyDTO readOnlyDTO = teacherService.insertTeacher(dto);
        return Response.created(uriInfo
                                    .getAbsolutePathBuilder()
                                    .path(readOnlyDTO.getId().toString()).build())
                                .entity(readOnlyDTO)
                                .build();
    }

    @PUT
    @Path("/{teacherId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateTeacher(@PathParam("teacherId") Long id, TeacherUpdateDTO dto) throws EntityInvalidArgumentException, EntityNotFoundException {
        List<String> errors = ValidatorUtil.validateDTO(dto);
        if (!errors.isEmpty()) {
            throw new EntityInvalidArgumentException("Teacher", String.join(",", errors));
        }
        TeacherReadOnlyDTO readOnlyDTO = teacherService.updateTeacher(dto);
        return Response.status(Response.Status.OK).entity(readOnlyDTO).build();
    }


    @DELETE
    @Path("/{teacherId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTeacher(@PathParam("teacherId") Long id) throws EntityNotFoundException {
        TeacherReadOnlyDTO readOnlyDTO = teacherService.getTeacherById(id);
        teacherService.deleteTeacher(id);
        return Response.status(Response.Status.OK).entity(readOnlyDTO).build();
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFilteredTeachers(@QueryParam("firstname") String firstname,
                                        @QueryParam("lastname") String lastname,
                                        @QueryParam("vat") String vat) {
        TeacherFiltersDTO filtersDTO = new TeacherFiltersDTO(firstname, lastname, vat);
        Map<String, Object> criteria = Mapper.mapToCriteria(filtersDTO);
        List<TeacherReadOnlyDTO> teacherReadOnlyDTOS = teacherService.getTeachersByCriteria(criteria);
        return Response.status(Response.Status.OK).entity(teacherReadOnlyDTOS).build();
    }
}
