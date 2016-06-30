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

package org.kie.dmn.feel11;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class FEELParser {

    public static ParseTree parse( String source ) {
        ANTLRInputStream input = new ANTLRInputStream(source);
        FEEL_1_1Lexer lexer = new FEEL_1_1Lexer( input );
        CommonTokenStream tokens = new CommonTokenStream( lexer );
        FEEL_1_1Parser parser = new FEEL_1_1Parser( tokens );
        ParseTree tree = parser.expression();
        return tree;
    }

}
