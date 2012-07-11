package edu.ualberta.med.biobank.common.action.csvimport;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.constraint.Unique;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCSVException;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.biobank.CommonBundle;
import edu.ualberta.med.biobank.common.action.Action;
import edu.ualberta.med.biobank.common.action.ActionContext;
import edu.ualberta.med.biobank.common.action.BooleanResult;
import edu.ualberta.med.biobank.common.action.exception.ActionException;
import edu.ualberta.med.biobank.common.action.patient.PatientSaveAction;
import edu.ualberta.med.biobank.i18n.Bundle;
import edu.ualberta.med.biobank.i18n.LString;
import edu.ualberta.med.biobank.i18n.Tr;
import edu.ualberta.med.biobank.model.PermissionEnum;
import edu.ualberta.med.biobank.model.Study;
import edu.ualberta.med.biobank.util.CompressedReference;

public class PatientCsvImportAction implements Action<BooleanResult> {
    private static final long serialVersionUID = 1L;

    private static final I18n i18n = I18nFactory
        .getI18n(SpecimenCsvImportAction.class);

    private static final Bundle bundle = new CommonBundle();

    @SuppressWarnings("nls")
    public static final String CSV_PARSE_ERROR =
        "Parse error at line {0}\n{1}";

    @SuppressWarnings("nls")
    public static final LString CSV_FILE_ERROR =
        bundle.tr("CVS file not loaded").format();

    @SuppressWarnings("nls")
    public static final Tr CSV_STUDY_ERROR =
        bundle.tr("CSV study {0} does not exist");

    @SuppressWarnings("nls")
    private static final CellProcessor[] PROCESSORS = new CellProcessor[] {
        null,
        new Unique(),
        new ParseDate("yyyy-MM-dd HH:mm")
    };

    private CompressedReference<ArrayList<PatientCsvInfo>> compressedList =
        null;

    private ActionContext context = null;

    public PatientCsvImportAction(String filename) throws IOException {
        setCsvFile(filename);
    }

    @SuppressWarnings("nls")
    private void setCsvFile(String filename) throws IOException {
        ICsvBeanReader reader = new CsvBeanReader(
            new FileReader(filename), CsvPreference.EXCEL_PREFERENCE);

        final String[] header = new String[] {
            "studyName",
            "patientNumber",
            "createdAt"
        };

        try {
            ArrayList<PatientCsvInfo> patientCsvInfos =
                new ArrayList<PatientCsvInfo>(0);

            PatientCsvInfo patientCsvInfo;
            reader.getCSVHeader(true);
            while ((patientCsvInfo =
                reader.read(PatientCsvInfo.class, header, PROCESSORS)) != null) {
                patientCsvInfos.add(patientCsvInfo);
            }

            compressedList =
                new CompressedReference<ArrayList<PatientCsvInfo>>(
                    patientCsvInfos);

        } catch (SuperCSVException e) {
            throw new IllegalStateException(
                i18n.tr(CSV_PARSE_ERROR, e.getMessage(), e.getCsvContext()));
        } finally {
            reader.close();
        }
    }

    @Override
    public boolean isAllowed(ActionContext context) throws ActionException {
        return PermissionEnum.LEGACY_IMPORT_CSV.isAllowed(context.getUser());
    }

    @Override
    public BooleanResult run(ActionContext context) throws ActionException {
        if (compressedList == null) {
            throw new ActionException(CSV_FILE_ERROR);
        }

        this.context = context;
        boolean result = false;

        ArrayList<PatientCsvInfo> patientCsvInfos = compressedList.get();
        for (PatientCsvInfo csvInfo : patientCsvInfos) {
            addPatient(csvInfo);
        }

        return new BooleanResult(result);
    }

    private void addPatient(PatientCsvInfo csvInfo) {
        Study study = loadStudy(csvInfo.getStudyName());

        PatientSaveAction patientSaveAction = new PatientSaveAction(
            null, study.getId(), csvInfo.getPatientNumber(),
            csvInfo.getCreatedAt(), null);
        patientSaveAction.run(context);
    }

    /*
     * Generates an action exception if specimen type does not exist.
     */
    @SuppressWarnings("nls")
    private Study loadStudy(String nameShort) {
        Criteria c = context.getSession()
            .createCriteria(Study.class, "st")
            .add(Restrictions.eq("nameShort", nameShort));

        Study study = (Study) c.uniqueResult();
        if (study == null) {
            throw new ActionException(CSV_STUDY_ERROR.format(nameShort));
        }
        return study;
    }

}