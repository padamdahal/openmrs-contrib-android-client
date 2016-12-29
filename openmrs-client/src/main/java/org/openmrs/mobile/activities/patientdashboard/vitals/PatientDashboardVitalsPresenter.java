/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.mobile.activities.patientdashboard.vitals;

import org.openmrs.mobile.activities.patientdashboard.PatientDashboardContract;
import org.openmrs.mobile.activities.patientdashboard.PatientDashboardMainPresenterImpl;
import org.openmrs.mobile.api.retrofit.VisitApi;
import org.openmrs.mobile.dao.EncounterDAO;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.listeners.retrofit.DefaultResponseCallbackListener;
import org.openmrs.mobile.models.Encounter;
import org.openmrs.mobile.utilities.NetworkUtils;

public class PatientDashboardVitalsPresenter extends PatientDashboardMainPresenterImpl implements PatientDashboardContract.PatientVitalsPresenter {

    private EncounterDAO encounterDAO;
    private PatientDashboardContract.ViewPatientVitals mPatientVitalsView;

    public PatientDashboardVitalsPresenter(String id, PatientDashboardContract.ViewPatientVitals mPatientVitalsView) {
        this.mPatient = new PatientDAO().findPatientByID(id);
        this.mPatientVitalsView = mPatientVitalsView;
        this.mPatientVitalsView.setPresenter(this);
        this.encounterDAO = new EncounterDAO();
    }

    @Override
    public void start() {
        loadVitalsFromDB();
        loadVitalsFromServer();
    }

    private void loadVitalsFromServer() {
        if (NetworkUtils.isOnline()) {
            new VisitApi().syncLastVitals(mPatient.getUuid(), new DefaultResponseCallbackListener() {
                @Override
                public void onResponse() {
                    loadVitalsFromDB();
                }

                @Override
                public void onErrorResponse(String errorMessage) {
                    mPatientVitalsView.showErrorToast(errorMessage);
                }
            });
        }
    }

    private void loadVitalsFromDB() {
        Encounter mVitalsEncounter = encounterDAO.getLastVitalsEncounter(mPatient.getUuid());
        if (mVitalsEncounter != null) {
            mPatientVitalsView.showEncounterVitals(mVitalsEncounter);
        }
        else {
            mPatientVitalsView.showNoVitalsNotification();
        }
    }

    @Override
    public void startFormDisplayActivityWithEncounter() {
        Encounter lastVitalsEncounter = encounterDAO.getLastVitalsEncounter(mPatient.getUuid());
        mPatientVitalsView.startFormDisplayActivity(lastVitalsEncounter);
    }
}
