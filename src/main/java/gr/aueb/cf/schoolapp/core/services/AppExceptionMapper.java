package gr.aueb.cf.schoolapp.core.services;

import gr.aueb.cf.schoolapp.core.exceptions.*;
import gr.aueb.cf.schoolapp.dto.ResponseMessageDTO;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AppExceptionMapper implements ExceptionMapper<EntityGenericException> {

    @Override
    public Response toResponse(EntityGenericException e) {
        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        if (e instanceof EntityNotFoundException) {
            status = Response.Status.NOT_FOUND;
        } else if (e instanceof EntityAlreadyExistsException) {
            status = Response.Status.CONFLICT;
        } else if (e instanceof EntityInvalidArgumentException) {
            status = Response.Status.BAD_REQUEST;
        } else if (e instanceof EntityNotAuthorizedException) {
            status = Response.Status.UNAUTHORIZED;
        } else if (e instanceof AppServerException) {
            status = Response.Status.SERVICE_UNAVAILABLE;
        }
        return Response.status(status)
                .entity(new ResponseMessageDTO(e.getCode(), e.getMessage()))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }
}
