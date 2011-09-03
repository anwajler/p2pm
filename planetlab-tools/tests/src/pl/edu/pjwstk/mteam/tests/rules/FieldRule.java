package pl.edu.pjwstk.mteam.tests.rules;

public class FieldRule {

    private String fieldName;
    private String fieldDescription;
    private Class fieldClass;

    public FieldRule(String fieldName, String fieldDescription, Class fieldClass) {
        this.fieldName = fieldName;
        this.fieldDescription = fieldDescription;
        this.fieldClass = fieldClass;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public String getFieldDescription() {
        return this.fieldDescription;
    }

    public Class getFieldClass() {
        return this.fieldClass;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof FieldRule && this.fieldName.equals(((FieldRule) o).getFieldName());
    }

    @Override
    public String toString() {
        StringBuilder strb = new StringBuilder("FieldRule=[name=\"");
        strb.append(this.fieldName).append("\", description=\"").append(this.fieldDescription).append("\", class=\"").append(this.fieldClass);
        strb.append("\"]");
        return strb.toString();
    }

}
