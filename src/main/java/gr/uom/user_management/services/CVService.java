package gr.uom.user_management.services;

import gr.uom.user_management.models.Skill;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CVService {

    public List<Skill> analyzeCV() {
        List<Skill> skillList = new ArrayList<>();
        skillList.add(new Skill("http://data.europa.eu/esco/skill/19a8293b-8e95-4de3-983f-77484079c389","Java (computer programming)"));
        skillList.add(new Skill("http://data.europa.eu/esco/skill/3cd569a2-4f88-4c1e-9995-8dce8c5e51a7","JavaScript"));
        skillList.add(new Skill("http://data.europa.eu/esco/skill/7369f779-4b71-4aab-8836-48b69c676eec","operate relational database management system"));

        return skillList;
    }
}
