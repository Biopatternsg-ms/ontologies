package com.biopatternsg.application.services;


import com.biopatternsg.domain.model.GoTermDTO;
import com.biopatternsg.domain.model.PathsToRoot;

import java.util.List;

public interface GeneOntologyTermService {
    PathsToRoot getPathsToRoot(String goId);
    List<GoTermDTO> getByIdsInWeb(List<String> ids);
    List<GoTermDTO> getByIdsInDB(List<String> ids);
}
