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

package org.kie.dmn.core.ast;

import org.kie.dmn.core.api.DMNContext;
import org.kie.dmn.core.api.DMNType;
import org.kie.dmn.core.api.event.InternalDMNRuntimeEventManager;
import org.kie.dmn.core.impl.DMNContextImpl;
import org.kie.dmn.core.impl.DMNResultImpl;
import org.kie.dmn.feel.lang.EvaluationContext;
import org.kie.dmn.feel.model.v1_1.FunctionDefinition;
import org.kie.dmn.feel.runtime.decisiontables.DecisionTableImpl;
import org.kie.dmn.feel.runtime.functions.BaseFEELFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class DMNExpressionEvaluatorInvokerFunction implements DMNExpressionEvaluator {
    private static final Logger logger = LoggerFactory.getLogger( DMNExpressionEvaluatorInvokerFunction.class );

    private final String name;
    private final FunctionDefinition functionDefinition;
    private List<FormalParameter> parameters = new ArrayList<>(  );
    private DMNExpressionEvaluator evaluator;

    public DMNExpressionEvaluatorInvokerFunction(String name, FunctionDefinition fdef ) {
        this.name = name;
        this.functionDefinition = fdef;
    }

    public List<List<String>> getParameterNames() {
        return Collections.singletonList( parameters.stream().map( p -> p.name ).collect( Collectors.toList()) );
    }

    public List<List<DMNType>> getParameterTypes() {
        return Collections.singletonList( parameters.stream().map( p -> p.type ).collect( Collectors.toList()) );
    }

    public void addParameter(String name, DMNType dmnType) {
        this.parameters.add( new FormalParameter( name, dmnType ) );
    }

    public void setEvaluator(DMNExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public DMNExpressionEvaluator getEvaluator() {
        return this.evaluator;
    }

    @Override
    public EvaluatorResult evaluate(InternalDMNRuntimeEventManager eventManager, DMNResultImpl result) {
        // when this evaluator is executed, it should return a "FEEL function" to register in the context
        DMNExpressionEvaluatorFunction function = new DMNExpressionEvaluatorFunction( name, parameters, evaluator, eventManager, result );
        return new EvaluatorResult( function, ResultType.SUCCESS );
    }

    private static class FormalParameter {
        final String name;
        final DMNType type;

        public FormalParameter(String name, DMNType type) {
            this.name = name;
            this.type = type;
        }
    }

    public static class DMNExpressionEvaluatorFunction extends BaseFEELFunction {
        private final List<FormalParameter> parameters;
        private final DMNExpressionEvaluator evaluator;
        private final InternalDMNRuntimeEventManager eventManager;
        private final DMNResultImpl resultContext;

        public DMNExpressionEvaluatorFunction(String name, List<FormalParameter> parameters, DMNExpressionEvaluator evaluator, InternalDMNRuntimeEventManager eventManager, DMNResultImpl result) {
            super( name );
            this.parameters = parameters;
            this.evaluator = evaluator;
            this.eventManager = eventManager;
            this.resultContext = result;
        }

        public Object invoke(EvaluationContext ctx, Object[] params) {
            DMNContext previousContext = resultContext.getContext();
            try {
                DMNContextImpl dmnContext = new DMNContextImpl();
                for( int i = 0; i < params.length; i++ ) {
                    dmnContext.set( parameters.get( i ).name, params[i] );
                }
                resultContext.setContext( dmnContext );
                EvaluatorResult result = evaluator.evaluate( eventManager, resultContext );
                if( result.getResultType() == ResultType.SUCCESS ) {
                    return result.getResult();
                }
                // TODO: are errors reported in the resultContext already or do we need additional treatment?
                return null;
            } catch ( Exception e ) {
                logger.error( "Error invoking expression for node '" + getName() + "'.", e );
                throw e;
            } finally {
                resultContext.setContext( previousContext );
            }
        }

        @Override
        protected boolean isCustomFunction() {
            return true;
        }

        public List<List<String>> getParameterNames() {
            return Collections.singletonList( parameters.stream().map( p -> p.name ).collect( Collectors.toList()) );
        }

        public List<List<DMNType>> getParameterTypes() {
            return Collections.singletonList( parameters.stream().map( p -> p.type ).collect( Collectors.toList()) );
        }
    }

}
