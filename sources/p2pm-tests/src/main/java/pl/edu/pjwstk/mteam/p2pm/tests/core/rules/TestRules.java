package pl.edu.pjwstk.mteam.p2pm.tests.core.rules;

import org.apache.log4j.Logger;

import java.util.Hashtable;
import java.util.Map;

public class TestRules implements IRules {

    public static final Logger LOG = Logger.getLogger(TestRules.class);

    private Map<String, FieldRule> fieldsRules = new Hashtable<String, FieldRule>();

    public TestRules() {}

    public TestRules(Map<String,FieldRule> fieldsRules) {
        this.fieldsRules = fieldsRules;
    }

    public void putFieldRule(String name, FieldRule fieldRule) {
        this.fieldsRules.put(name, fieldRule);
    }

    public void removeFieldRule(String name) {
        this.fieldsRules.remove(name);
    }

    public void setFieldRule(Map<String,FieldRule> fieldRules) {
        this.fieldsRules = fieldsRules;
    }

    public Map<String, FieldRule> getFieldsRules() {
        return this.fieldsRules;
    }

    public int getFieldsRulesCount() {
        return this.fieldsRules.size();
    }

    public boolean verifyField(String key, Object value) {
        if (LOG.isTraceEnabled()) LOG.trace("Veryfing field key=" + key + " value=" + value);

        if (!this.fieldsRules.containsKey(key)) {
            if (LOG.isTraceEnabled()) LOG.trace("Ruleset for fields does not contain key " + key);
            return false;
        }

        Class classForKey = this.fieldsRules.get(key).getFieldClass();
        Class valuesKey = value.getClass();
        if (!classForKey.equals(valuesKey)) {
            if (LOG.isTraceEnabled()) LOG.trace("Ruleset for fields says value for key " + key + " must be "  + classForKey + " not " +valuesKey);
            return false;
        }

        return true;
    }

}
