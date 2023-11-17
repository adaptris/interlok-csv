package com.adaptris.core.transform.csvjson;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceImp;
import com.adaptris.csv.BasicPreferenceBuilder;
import com.adaptris.csv.PreferenceBuilder;

import lombok.Getter;
import lombok.Setter;

public abstract class CSVConverter extends ServiceImp {

  @NotNull
  @AutoPopulated
  @InputFieldDefault(value = "csv-basic-preference-builder")
  @Getter
  @Setter
  private PreferenceBuilder preferenceBuilder;

  public CSVConverter() {
    setPreferenceBuilder(new BasicPreferenceBuilder(BasicPreferenceBuilder.Style.STANDARD_PREFERENCE));
  }

  @Override
  public void prepare() throws CoreException {
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  @SuppressWarnings("unchecked")
  public <T extends CSVConverter> T withPreferenceBuilder(PreferenceBuilder p) {
    setPreferenceBuilder(p);
    return (T) this;
  }

}
