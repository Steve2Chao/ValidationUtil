
import java.io.ByteArrayInputStream;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.codecs.Codec;
import org.owasp.esapi.codecs.OracleCodec;
import org.owasp.html.Handler;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.HtmlSanitizer;
import org.owasp.html.HtmlStreamRenderer;

public class ValidationUtil extends Object {
	private static Codec oracleCodec = new OracleCodec();

	public static void main(String[] args) {

		String alphabet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String specialChar = "`~!@#$%^&*()_-+= {[}]|\\:;\"'<,>.?/";

		System.out.println("alphabet=" + alphabet);
		System.out.println("escNull=" + escNull(alphabet));

		System.out.println("specialChar=" + specialChar);
		System.out.println("escNull    =" + escNull(specialChar));

		System.out.println(encodeDatabaseInputAndSanitize("00001"));
		System.out.println(encodeDatabaseInputAndSanitize(new Integer("0002")));
		System.out.println(encodeDatabaseInputAndSanitize(new Integer("1003")));
		System.out.println(encodeDatabaseInputAndSanitize(1004));
		System.out.println("encodeDatabaseInputAndSanitize=" + encodeDatabaseInputAndSanitize(specialChar));

		System.out.println("=" + encodeDatabaseInput("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"));
		System.out.println("=" + encodeDatabaseInput("`~!@#$%^&*()-_+=\"{}[]|:;'<>,.?/"));

		System.out.println("blank" + sanitizeIntString(null) == null);

		String a = "";
		String d = "1";
		String b = null;
		String c = "1234567890123456789012345678901234567890";

		System.out.println(a == b); // false
		System.out.println(a.equals(b)); // false
		System.out.println(sanitizeIntString(a).equals("")); // false
		System.out.println(sanitizeIntString(c)); // false
		System.out.println(sanitizeIntString(a) == ""); // false
		System.out.println(sanitizeIntString(b) == null); // false
		System.out.println(sanitizeIntString(d) == "1"); // false
		System.out.println(sanitizeIntString(d).equals("1")); // false
		System.out.println("sanitize 1234=" + sanitize(new Integer(1234)));

		int i = -3;
		System.out.println("sanitize -3=" + sanitize(i));

	}

	public static String escNull(String str) {
		String s = "";
		if (str != null && !"null".equalsIgnoreCase(str)) {
			s = str;
		}
		return encodeDatabaseInput(s);
	}

	public static String makeNull(String b) {
		return b == null ? null : b;
	}

	private static String sanitizeNoElementContent(String html) {
		StringBuilder sb = new StringBuilder();
		HtmlStreamRenderer renderer = HtmlStreamRenderer.create(sb, new Handler<String>() {
			public void handle(String errorMessage) {
				System.out.println("error");
			}
		});
		HtmlSanitizer.Policy policy = new HtmlPolicyBuilder().allowElements("").build(renderer);
		HtmlSanitizer.sanitize(html, policy);
		return sb.toString();
	}

	public static String sanitizeAllowCommonFormatWithLinks(String html) {
		HtmlPolicyBuilder policyBuilderWithLinks = new HtmlPolicyBuilder().allowElements("a")
				.allowCommonInlineFormattingElements().allowAttributes("title").globally()
				.allowAttributes("href", "target", "tabindex").onElements("a").allowStandardUrlProtocols();
		StringBuilder sb = new StringBuilder();
		HtmlStreamRenderer renderer = HtmlStreamRenderer.create(sb, new Handler<String>() {
			public void handle(String errorMessage) {
				System.out.println("error");
			}
		});
		HtmlSanitizer.Policy policy = policyBuilderWithLinks.allowCommonInlineFormattingElements().build(renderer);
		HtmlSanitizer.sanitize(html, policy);
		return sb.toString();

	}

	public static String sanitize(String html) {
		if (html == null)
			return null;
		return sanitizeAllowCommonFormatWithLinks(html);
	}

	public static Integer sanitize(Integer html) {
		if (html == null)
			return null;
		return Integer.valueOf(sanitize(html.toString()));
	}

	public static int sanitize(int html) {
		return Integer.parseInt(sanitize(String.valueOf(html)));
	}

	public static String encodeDatabaseInput(String input) {
		if (input == null)
			return null;
		input = input.replaceAll("''", "'");
		return ESAPI.encoder().encodeForSQL(oracleCodec, input);

	}

	public static String encodeDatabaseInputAndSanitize(String input) {
		if (input == null)
			return null;
		input = input.replaceAll("''", "'");
		return encodeDatabaseInput(sanitize(input));

	}

	public static int encodeDatabaseInputAndSanitize(int input) {
		return Integer.parseInt(String.valueOf(input));
	}

	public static Integer encodeDatabaseInputAndSanitize(Integer input) {
		return input == null ? null : Integer.parseInt((String.valueOf(input)));
	}

	public static String sanitizeIntString(String input) {
		return input == null ? null : (input.contentEquals("") ? "" : new java.math.BigInteger(input).toString());
	}

	public static boolean unsafeExcel(byte[] excel) {
		if (excel != null) {
			ByteArrayInputStream iStream = new ByteArrayInputStream(excel);
			try {
				Workbook wb = WorkbookFactory.create(iStream);
				for (int i = 0; i < wb.getNumberOfSheets(); i++) {
					Sheet sheet = wb.getSheetAt(i);
					Iterator<Row> itr = sheet.iterator();
					while (itr.hasNext()) {
						Row row = itr.next();
						Iterator<Cell> cellIterator = row.cellIterator();
						while (cellIterator.hasNext()) {
							Cell cell = cellIterator.next();
							switch (cell.getCellType().getCode()) {
							case 1: // string
								if (cell.getStringCellValue().toUpperCase().contains("CMD")
										|| cell.getStringCellValue().toUpperCase().contains("WEBSERVICE"))
									return true;
								break;
							case 2: // formula
								if (cell.getCellFormula().toUpperCase().contains("CMD")
										|| cell.getCellFormula().toUpperCase().contains("WEBSERVICE"))
									return true;
								break;
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

}
