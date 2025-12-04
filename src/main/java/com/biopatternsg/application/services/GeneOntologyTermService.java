package com.biopatternsg.application.services;


import com.biopatternsg.domain.model.GoTerm;
import com.biopatternsg.domain.model.PathsToRoot;

import java.util.List;

public interface GeneOntologyTermService {
    PathsToRoot getPathsToRoot(String goId);
    List<GoTerm> getByIdsInWeb(List<String> ids);
    List<GoTerm> getByIdsInDB(List<String> ids);
}
