/*
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.ide.tests.testlanguage.ide

import com.google.inject.Provider
import java.util.concurrent.ExecutorService

/**
 * Use this class to register ide components.
 */
class TestLanguageIdeModule extends AbstractTestLanguageIdeModule {

	new() {
		super()
	}

	new(Provider<ExecutorService> executorServiceProvider) {
		super(executorServiceProvider)
	}
	
}
