package com.R.microservice;

import java.io.IOException;
import java.nio.file.Paths;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;

@RestController
public class RServiceController {

	@RequestMapping("/")
	public String index() {
		return "R microservice is running";
	}

	@RequestMapping(value = "/plot/{file}", produces = "image/png", method=RequestMethod.GET)
	public byte[] evaluate(@RequestParam(value = "template", required=true)String template, @PathVariable String file) {
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
			
			String debugCairoString = "try("+device+"('test.png',quality=90))";
			REXP expression = connection.eval(debugCairoString);
			if (expression.inherits("try-error")) {
				System.out.println("Can't open" + device + "graphics device:\n"
						+ expression.asString());
				REXP warning = connection
						.eval("if (exists('last.warning') && length(last.warning)>0) names(last.warning)[1] else 0");
				if (warning.isString())
					System.out.println(warning.asString());
				return new byte[0];
			}
			String filePathString = Paths.get(FileUploadController.SERVER_DATA_DIR, file).toString();
			connection.parseAndEval(file+" = read.csv("+"'"+filePathString+".csv')");
			connection.parseAndEval(template);
			
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
	
	@RequestMapping(value = "/listOfServices")
	public String listOfServices() throws IOException{
		
		byte[] rModulejsonData = ByteStreams.toByteArray(this.getClass().getResourceAsStream("listofservices.txt"));
		
		ObjectMapper objectMapper = new ObjectMapper();
		RRoot rmodule = objectMapper.readValue(rModulejsonData, RRoot.class);
		return objectMapper.writeValueAsString(rmodule);
	}

}
