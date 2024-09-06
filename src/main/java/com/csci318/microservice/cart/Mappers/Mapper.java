package com.csci318.microservice.cart.Mappers;

import java.util.List;


public interface Mapper<Entities, DTOsResponse, DTOsRequest> {
    DTOsResponse toDtos(Entities entity);
    Entities toEntities(DTOsRequest dto);
    List<DTOsResponse> toDtos(List<Entities> entities);
    List<Entities> toEntities(List<DTOsRequest> dtos);
}
