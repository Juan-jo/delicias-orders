package org.delicias.delivery_users.dto;

import jakarta.validation.constraints.NotNull;
import org.delicias.common.validation.OnUpdate;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public class UpdateDeliveryUserReqDTO {

    @RestForm("id")
    @NotNull(message = "The parameter is mandatory", groups = { OnUpdate.class})
    public Integer id;

    @RestForm("name")
    @NotNull(message = "The parameter is mandatory", groups = { OnUpdate.class})
    public String name;

    @RestForm("lastName")
    @NotNull(message = "The parameter is mandatory", groups = { OnUpdate.class})
    public String lastName;

    @RestForm("email")
    @NotNull(message = "The parameter is mandatory", groups = { OnUpdate.class})
    public String email;

    public FileUpload picture;
}
