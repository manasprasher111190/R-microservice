package main.java;

import java.util.Arrays;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

	@RequestMapping("/")
	public String index() {
		return "Greetings from Spring Boot!";
	}

	@RequestMapping(value = "/plot", produces = "image/png")
	public byte[] evaluate() {
		RConnection connection = null;
		try {
			String device = "png";
			/*
			 * Create a connection to Rserve instance running on default port
			 * 6311
			 */
			connection = new RConnection();
			if (connection.parseAndEval(
					"suppressWarnings(require('Cairo',quietly=TRUE))")
					.asInteger() > 0) {
				device = "CairoPNG";
			} else {
				System.out
						.println("(Consider installing Cairo.package for better bitmap output)");
			}
			
			REXP expression = connection.eval("try("+device+"('test.png',quality=90))");
			if (expression.inherits("try-error")) {
				System.out.println("Can't open" + device + "graphics device:\n"
						+ expression.asString());
				REXP warning = connection
						.eval("if (exists('last.warning') && length(last.warning)>0) names(last.warning)[1] else 0");
				if (warning.isString())
					System.out.println(warning.asString());
				return new byte[0];
			}
			
//			connection
//					.parseAndEval("data(iris); attach(iris); plot(Sepal.Length, Petal.Length, col=unclass(Species)); dev.off()");
//			
			connection.parseAndEval("plot(c(1,2,3,4))");
			connection.parseAndEval("dev.off()");
			 expression = connection.parseAndEval("r=readBin('test.png','raw',1024*1024); unlink('test.png'); r");
			 return expression.asBytes();

		} catch (RserveException e) {
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			e.printStackTrace();
		} catch (REngineException e) {
			e.printStackTrace();
		}
		return new byte[0];
	}

}
