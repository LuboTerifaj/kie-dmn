/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.dmn.core;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import org.junit.Test;
import org.kie.dmn.core.api.DMNContext;
import org.kie.dmn.core.api.DMNFactory;
import org.kie.dmn.core.api.DMNModel;
import org.kie.dmn.core.api.DMNResult;
import org.kie.dmn.core.api.DMNRuntime;
import org.kie.dmn.core.util.DMNRuntimeUtil;

public class DMNDecisionTableRuntimeTest {

    @Test
    public void testSimpleDecisionTableUniqueHitPolicy() {
        DMNRuntime runtime = DMNRuntimeUtil.createRuntime( "0004-simpletable-U.dmn", this.getClass() );
        DMNModel dmnModel = runtime.getModel( "https://github.com/droolsjbpm/kie-dmn", "0004-simpletable-U" );
        assertThat( dmnModel, notNullValue() );

        DMNContext context = DMNFactory.newContext();
        context.set( "Age", new BigDecimal( 18 ) );
        context.set( "RiskCategory", "Medium" );
        context.set( "isAffordable", true );

        DMNResult dmnResult = runtime.evaluateAll( dmnModel, context );

        DMNContext result = dmnResult.getContext();

        assertThat( result.get( "Approval Status" ), is( "Approved" ) );
    }

    @Test
    public void testSimpleDecisionTableUniqueHitPolicySatisfies() {
        DMNRuntime runtime = DMNRuntimeUtil.createRuntime( "0004-simpletable-U.dmn", this.getClass() );
        DMNModel dmnModel = runtime.getModel( "https://github.com/droolsjbpm/kie-dmn", "0004-simpletable-U" );
        assertThat( dmnModel, notNullValue() );

        DMNContext context = DMNFactory.newContext();
        context.set( "Age", new BigDecimal( 18 ) );
        context.set( "RiskCategory", "ASD" );
        context.set( "isAffordable", false );

        DMNResult dmnResult = runtime.evaluateAll( dmnModel, context );

        DMNContext result = dmnResult.getContext();

        assertThat( result.get( "Approval Status" ), nullValue() );
        assertTrue( dmnResult.getMessages().size() > 0 );
    }

    @Test
    public void testDecisionTableWithCalculatedResult() {
        DMNRuntime runtime = DMNRuntimeUtil.createRuntime( "calculation1.dmn", this.getClass() );
        DMNModel dmnModel = runtime.getModel( "http://www.trisotech.com/definitions/_77ae284e-ce52-4579-a50f-f3cc584d7f4b", "Calculation1" );
        assertThat( dmnModel, notNullValue() );

        DMNContext context = DMNFactory.newContext();
        context.set( "MonthlyDeptPmt", BigDecimal.valueOf( 200 ) );
        context.set( "MonthlyPmt", BigDecimal.valueOf( 100 ) );
        context.set( "MonthlyIncome", BigDecimal.valueOf( 600 ) );

        DMNResult dmnResult = runtime.evaluateAll(dmnModel, context);
        assertThat( dmnResult.hasErrors(), is( false ) );

        DMNContext result = dmnResult.getContext();
        assertThat( ((BigDecimal)result.get("Logique de décision 1")).setScale( 1, RoundingMode.CEILING), is( BigDecimal.valueOf( 0.5 ) ) );
    }

    @Test
    public void testDecisionTableMultipleResults() {
        DMNRuntime runtime = DMNRuntimeUtil.createRuntime( "car_damage_responsibility.dmn", this.getClass() );
        DMNModel dmnModel = runtime.getModel( "http://www.trisotech.com/definitions/_820611e9-c21c-47cd-8e52-5cba2be9f9cc", "Car Damage Responsibility" );
        assertThat( dmnModel, notNullValue() );

        DMNContext context = DMNFactory.newContext();
        context.set( "Membership Level", "Silver" );
        context.set( "Damage Types", "Body" );
        context.set( "Responsible", "Driver" );

        DMNResult dmnResult = runtime.evaluateAll(dmnModel, context);
        assertThat( dmnResult.hasErrors(), is( false ) );

        DMNContext result = dmnResult.getContext();
        assertThat( (Map<String,Object>)result.get("Car Damage Responsibility"), hasEntry( is( "EU Rent" ), is( BigDecimal.valueOf( 40 )) ));
        assertThat( (Map<String,Object>)result.get("Car Damage Responsibility"), hasEntry( is( "Renter" ), is( BigDecimal.valueOf( 60 )) ));
        assertThat( result.get("Payment method"), is( "Check" ) );
    }

    @Test
    public void testDecisionTableUniqueHitPolicy() {
        DMNRuntime runtime = DMNRuntimeUtil.createRuntime( "BranchDistribution.dmn", this.getClass() );
        DMNModel dmnModel = runtime.getModel( "http://www.trisotech.com/dmn/definitions/_cdf29af2-959b-4004-8271-82a9f5a62147", "Dessin 1" );
        assertThat( dmnModel, notNullValue() );

        DMNContext context = DMNFactory.newContext();
        context.set( "Branches dispersion", "Province" );
        context.set( "Number of Branches", BigDecimal.valueOf( 10 ) );

        DMNResult dmnResult = runtime.evaluateAll(dmnModel, context);
        assertThat( dmnResult.hasErrors(), is( false ) );

        DMNContext result = dmnResult.getContext();
        System.out.println(result);
        assertThat( result.get("Branches distribution"), is( "Medium" ) );
    }

    @Test
    public void testDecisionTableCollectHitPolicy() {
        DMNRuntime runtime = DMNRuntimeUtil.createRuntime( "Collect_Hit_Policy.dmn", this.getClass() );
        DMNModel dmnModel = runtime.getModel( "http://www.trisotech.com/definitions/_da1a4dcb-01bf-4dee-9be8-f498bc68178c", "Collect Hit Policy" );
        assertThat( dmnModel, notNullValue() );

        DMNContext context = DMNFactory.newContext();
        context.set( "Input", 20 );

        DMNResult dmnResult = runtime.evaluateAll(dmnModel, context);
        assertThat( dmnResult.hasErrors(), is( false ) );

        DMNContext result = dmnResult.getContext();
        assertThat( result.get("Collect"), is( BigDecimal.valueOf( 50 ) ) );
    }

    @Test
    public void testDecisionTableInvalidInputErrorMessage() {
        DMNRuntime runtime = DMNRuntimeUtil.createRuntime( "InvalidInput.dmn", this.getClass() );
        DMNModel dmnModel = runtime.getModel( "http://www.trisotech.com/dmn/definitions/_cdf29af2-959b-4004-8271-82a9f5a62147", "Dessin 1" );
        assertThat( dmnModel, notNullValue() );

        DMNContext context = DMNFactory.newContext();
        context.set( "Branches dispersion", "Province" );
        context.set( "Number of Branches", BigDecimal.valueOf( 10 ) );

        DMNResult dmnResult = runtime.evaluateAll(dmnModel, context);
        assertThat( dmnResult.hasErrors(), is( true ) );

        DMNContext result = dmnResult.getContext();
        assertThat( result.isDefined("Branches distribution"), is( false ) );
    }
}
