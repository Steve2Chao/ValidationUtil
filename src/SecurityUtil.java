import org.owasp.html.Handler;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.HtmlStreamRenderer;
import org.owasp.html.HtmlSanitizer;

public class SecurityUtil {
	
	public static String sanitizeAllowCommonFormat(String content) {
		HtmlPolicyBuilder policyBuilder = new HtmlPolicyBuilder().allowCommonInlineFormattingElements();
		return sanitize(content, policyBuilder);
	}
	
	public static String sanitize(String content, HtmlPolicyBuilder policyBuilder) {
		//test add a line  FirstBranch
		StringBuilder sb = new StringBuilder();
		HtmlStreamRenderer renderer = HtmlStreamRenderer.create(sb, new Handler<String>() {
			public void handle(String errorMessage) {
				System.out.println("Error occurred while attempting to render content: "+errorMessage);
			}
		});
		
		HtmlSanitizer.Policy policy = policyBuilder.toFactory().apply(renderer);
		HtmlSanitizer.sanitize(content, policy);
		
		return sb.toString();
	}
}
