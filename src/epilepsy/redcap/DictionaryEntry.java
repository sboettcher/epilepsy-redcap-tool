package epilepsy.redcap;

import com.opencsv.bean.CsvBindByName;

public class DictionaryEntry {
  @CsvBindByName(column = "Variable / Field Name", required = true)
  private String fieldName;

  @CsvBindByName(column = "Form Name", required = true)
  private String formName;

  @CsvBindByName(column = "Section Header")
  private String sectionHeader;

  @CsvBindByName(column = "Field Type", required = true)
  private String fieldType;

  @CsvBindByName(column = "Field Label")
  private String fieldLabel;

  @CsvBindByName(column = "Choices, Calculations, OR Slider Labels")
  private String choicesCalcsSlider;

  @CsvBindByName(column = "Field Note")
  private String fieldNote;

  @CsvBindByName(column = "Text Validation Type OR Show Slider Number")
  private String textValidationType;

  @CsvBindByName(column = "Text Validation Min")
  private String textValidationMin;

  @CsvBindByName(column = "Text Validation Max")
  private String textValidationMax;

  @CsvBindByName(column = "Identifier?")
  private String identifier;

  @CsvBindByName(column = "Branching Logic (Show field only if...)")
  private String branchingLogic;

  @CsvBindByName(column = "Required Field?")
  private String fieldRequired;

  @CsvBindByName(column = "Custom Alignment")
  private String customAlignment;

  @CsvBindByName(column = "Question Number (surveys only)")
  private String questionNumber;

  @CsvBindByName(column = "Matrix Group Name")
  private String matrixGroupName;

  @CsvBindByName(column = "Matrix Ranking?")
  private String matrixRanking;

  @CsvBindByName(column = "Field Annotation")
  private String fieldAnnotation;



  /*
   * Getter/Setter, generated
   */

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public String getFormName() {
    return formName;
  }

  public void setFormName(String formName) {
    this.formName = formName;
  }

  public String getSectionHeader() {
    return sectionHeader;
  }

  public void setSectionHeader(String sectionHeader) {
    this.sectionHeader = sectionHeader;
  }

  public String getFieldType() {
    return fieldType;
  }

  public void setFieldType(String fieldType) {
    this.fieldType = fieldType;
  }

  public String getFieldLabel() {
    return fieldLabel;
  }

  public void setFieldLabel(String fieldLabel) {
    this.fieldLabel = fieldLabel;
  }

  public String getChoicesCalcsSlider() {
    return choicesCalcsSlider;
  }

  public void setChoicesCalcsSlider(String choicesCalcsSlider) {
    this.choicesCalcsSlider = choicesCalcsSlider;
  }

  public String getFieldNote() {
    return fieldNote;
  }

  public void setFieldNote(String fieldNote) {
    this.fieldNote = fieldNote;
  }

  public String getTextValidationType() {
    return textValidationType;
  }

  public void setTextValidationType(String textValidationType) {
    this.textValidationType = textValidationType;
  }

  public String getTextValidationMin() {
    return textValidationMin;
  }

  public void setTextValidationMin(String textValidationMin) {
    this.textValidationMin = textValidationMin;
  }

  public String getTextValidationMax() {
    return textValidationMax;
  }

  public void setTextValidationMax(String textValidationMax) {
    this.textValidationMax = textValidationMax;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getBranchingLogic() {
    return branchingLogic;
  }

  public void setBranchingLogic(String branchingLogic) {
    this.branchingLogic = branchingLogic;
  }

  public String getFieldRequired() {
    return fieldRequired;
  }

  public void setFieldRequired(String fieldRequired) {
    this.fieldRequired = fieldRequired;
  }

  public String getCustomAlignment() {
    return customAlignment;
  }

  public void setCustomAlignment(String customAlignment) {
    this.customAlignment = customAlignment;
  }

  public String getQuestionNumber() {
    return questionNumber;
  }

  public void setQuestionNumber(String questionNumber) {
    this.questionNumber = questionNumber;
  }

  public String getMatrixGroupName() {
    return matrixGroupName;
  }

  public void setMatrixGroupName(String matrixGroupName) {
    this.matrixGroupName = matrixGroupName;
  }

  public String getMatrixRanking() {
    return matrixRanking;
  }

  public void setMatrixRanking(String matrixRanking) {
    this.matrixRanking = matrixRanking;
  }

  public String getFieldAnnotation() {
    return fieldAnnotation;
  }

  public void setFieldAnnotation(String fieldAnnotation) {
    this.fieldAnnotation = fieldAnnotation;
  }
}
