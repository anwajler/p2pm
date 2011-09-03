package pl.edu.pjwstk.mteam.tests.rules;

import java.util.Map;

public interface IRules {

    public Map<String,FieldRule> getFieldsRules();

    public boolean verifyField(String key, Object value);

}
