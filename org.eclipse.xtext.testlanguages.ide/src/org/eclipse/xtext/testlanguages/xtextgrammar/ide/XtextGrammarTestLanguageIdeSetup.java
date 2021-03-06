/*
 * generated by Xtext
 */
package org.eclipse.xtext.testlanguages.xtextgrammar.ide;

import org.eclipse.xtext.testlanguages.xtextgrammar.XtextGrammarTestLanguageRuntimeModule;
import org.eclipse.xtext.testlanguages.xtextgrammar.XtextGrammarTestLanguageStandaloneSetup;
import org.eclipse.xtext.util.Modules2;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Initialization support for running Xtext languages without Equinox extension registry.
 */
public class XtextGrammarTestLanguageIdeSetup extends XtextGrammarTestLanguageStandaloneSetup {

	@Override
	public Injector createInjector() {
		return Guice.createInjector(Modules2.mixin(new XtextGrammarTestLanguageRuntimeModule(), new XtextGrammarTestLanguageIdeModule()));
	}
}
