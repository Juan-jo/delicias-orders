package org.delicias.delivery_users.dto;

import jakarta.validation.constraints.NotNull;
import org.delicias.common.validation.OnCreate;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public class CreateDeliverUserReqDTO {

    @RestForm("username")
    @NotNull(message = "The parameter is mandatory", groups = { OnCreate.class})
    public String username;

    @RestForm("email")
    @NotNull(message = "The parameter is mandatory", groups = { OnCreate.class})
    public String email;

    @RestForm("password")
    @NotNull(message = "The parameter is mandatory", groups = { OnCreate.class})
    public String password;

    @RestForm("name")
    @NotNull(message = "The parameter is mandatory", groups = { OnCreate.class})
    public String name;

    @RestForm("lastName")
    @NotNull(message = "The parameter is mandatory", groups = { OnCreate.class})
    public String lastName;

    @RestForm("zoneId")
    @NotNull(message = "The parameter is mandatory", groups = { OnCreate.class})
    public Integer zoneId;

    @RestForm("picture")
    public FileUpload picture;

}
