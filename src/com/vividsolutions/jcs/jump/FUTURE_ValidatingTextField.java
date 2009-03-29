package com.vividsolutions.jcs.jump;

import com.vividsolutions.jump.workbench.ui.ValidatingTextField;
import com.vividsolutions.jump.workbench.ui.ValidatingTextField.Cleaner;

public class FUTURE_ValidatingTextField {
	public static class BlankCleaner implements ValidatingTextField.Cleaner {
		private String replacement;

		public BlankCleaner(String replacement) {
			this.replacement = replacement;
		}

		public String clean(String text) {
			return (text.trim().length() == 0) ? getReplacement() : text;
		}

		protected String getReplacement() {
			return replacement;
		}
	}

	/**
	 * Leave untouched the really good stuff and the really bad stuff,
	 * but replace the sort-of good stuff, as it's probably just transient.
	 */
	public static class NumberCleaner implements ValidatingTextField.Cleaner {
		private String replacement;

		public NumberCleaner(String replacement) {
			this.replacement = replacement;
		}

		public String clean(String text) {
			try {
				Double.parseDouble(text.trim());
				return text.trim();
			} catch (NumberFormatException e) {
				try {
					//Handle -, ., E [Jon Aquino 2004-08-04]
					Double.parseDouble(text.trim() + "0");
					return replacement;
				} catch (NumberFormatException e2) {
					return text.trim();
				}
			}
		}

		protected String getReplacement() {
			return replacement;
		}
	}
}