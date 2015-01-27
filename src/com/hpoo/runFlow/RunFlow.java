package com.hpoo.runFlow;

import com.dynatrace.diagnostics.pdk.*;

import java.util.Collection;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

public class RunFlow implements Action {

	private static final Logger log = Logger.getLogger(RunFlow.class.getName());

	@Override
	public Status setup(ActionEnvironment env) throws Exception {
		return new Status(Status.StatusCode.Success);
	}

	@Override
	public Status execute(ActionEnvironment env) throws Exception {
		log.info("############################ START of HPOO Processing ###########################");
		String protocol = env.getConfigString("hpooProtocol");
		String hpooURL = env.getConfigString("hpooURL");
		String hpooPort = env.getConfigString("hpooPort");
		String flowPath = env.getConfigString("flowPath");
		String flowName = env.getConfigString("flowName");
		String flowParam = env.getConfigString("flowParameters");
		String UserName = env.getConfigString("hpooUser");
		String Password = env.getConfigPassword("hpooPass");
		String incidentHost = "";
		String guid = "";
		String agentName = "";
		String incidentName = "";
		String startTime = "";
		String severity = "";
		String systemProfile = "";
		String dynaTraceServer = "";
		String incidentInstance = "";
		String incidentInstanceType = "";
		String measureValue = "";
		String measureUnit = "";
		String thresholdValue = "";
		String incidentMessage = "";

		String URL = protocol + "://" + hpooURL + ":" + hpooPort
				+ "/PAS/services/rest/run_async";
		if (!flowPath.startsWith("/") && !flowPath.startsWith("\\")) {
			flowPath = "/" + flowPath;
		}
		if (!flowPath.endsWith("/") && !flowPath.endsWith("\\")) {
			flowPath = flowPath + "/";
		}
		URL += flowPath + flowName;
		log.fine("flowPath + Name: " + URL);
		URL = StringEscapeUtils.escapeHtml4(URL);
		// If Parameters call for incident host determine the hostname from the
		// incident
		if (flowParam != null && !flowParam.equals("")) {
			log.fine("Parameter Parsing Started");
			URL += "?";
			Collection<Incident> incidents = env.getIncidents();
			for (Incident i : incidents) {
				incidentMessage = i.getMessage();
				dynaTraceServer = i.getServerName();
				severity = getSeverityAsString(i);
				incidentName = i.getIncidentRule().getName();
				startTime = i.getStartTime().toString();
				guid = i.getKey().getUUID();
				systemProfile = env.getSystemProfileName();
				Collection<Violation> violations = i.getViolations();
				for (Violation v : violations) {
					Measure violatedMeasure = v.getViolatedMeasure();
					Source source = violatedMeasure.getSource();
					for (Violation.TriggerValue vt : v.getTriggerValues()) {
						measureValue = vt.getValue().toString().replaceAll("[a-zA-Z[%]]", "");
					}
					measureUnit = violatedMeasure.getUnit().toString();
					thresholdValue = v.getViolatedThreshold().getValue().toString().replace(".00", "");
					if (source.getSourceType() == SourceType.Monitor) {
						String sMeasure = violatedMeasure.getName();
						if(sMeasure.contains("@"))
						{
							log.finer("Measure: "+ sMeasure);
							incidentHost = StringUtils.substringAfter(sMeasure, "@");
							log.finer("Measure Host: "+ incidentHost);
						}
						String pat = "\\[.*-\\>(.*?)\\]";
						Pattern pattern = Pattern.compile(pat);
						Matcher matches = pattern.matcher(sMeasure);
						while (matches.find()) {
							// log.fine(matches.group());
							incidentInstanceType = StringUtils
									.substringBetween(sMeasure, "[", "->");
							// log.fine(incidentInstanceType);
							incidentInstance = StringUtils.substringBetween(
									sMeasure, "->", "]");
							// log.fine(incidentInstance);
						}

					} else if (source.getSourceType() == SourceType.Agent) {
						String AgentHostName = ((AgentSource) source).getHost().toString();
						agentName = ((AgentSource) source).getName().toString();
						log.fine("Agent type measure.");
						incidentHost = AgentHostName;
						
						// log.fine(incidentHost);
					}
				}
			}
			// Replace all variables
			if (flowParam.contains("{$host}")) {
				log.fine("*****Host Found!*****");
				Pattern p = Pattern.compile("(\\Q{$host}\\E)");
				Matcher matches = p.matcher(flowParam);
				flowParam = matches.replaceAll(incidentHost);
				log.fine(flowParam);
			}
			if (flowParam.contains("{$guid}")) {
				log.fine("*****GUID Found!*****");
				Pattern p = Pattern.compile("(\\Q{$guid}\\E)");
				Matcher matches = p.matcher(flowParam);
				flowParam = matches.replaceAll(guid);
				log.fine(flowParam);
			}
			if (flowParam.contains("{$agent}")) {
				log.fine("*****Agent Found!*****");
				Pattern p = Pattern.compile("(\\Q{$agent}\\E)");
				Matcher matches = p.matcher(flowParam);
				flowParam = matches.replaceAll(agentName);
				log.fine(flowParam);
			}
			if (flowParam.contains("{$incident}")) {
				log.fine("*****Incident Found!*****");
				Pattern p = Pattern.compile("(\\Q{$incident}\\E)");
				Matcher matches = p.matcher(flowParam);
				flowParam = matches.replaceAll(incidentName);
				log.fine(flowParam);
			}
			if (flowParam.contains("{$startTime}")) {
				log.fine("*****Start Time Found!*****");
				Pattern p = Pattern.compile("(\\Q{$startTime}\\E)");
				Matcher matches = p.matcher(flowParam);
				flowParam = matches.replaceAll(startTime);
				log.fine(flowParam);
			}
			if (flowParam.contains("{$severity}")) {
				log.fine("*****Severity Found!*****");
				Pattern p = Pattern.compile("(\\Q{$severity}\\E)");
				Matcher matches = p.matcher(flowParam);
				flowParam = matches.replaceAll(severity);
				log.fine(flowParam);
			}
			if (flowParam.contains("{$profile}")) {
				log.fine("*****Profile Found!*****");
				Pattern p = Pattern.compile("(\\Q{$profile}\\E)");
				Matcher matches = p.matcher(flowParam);
				flowParam = matches.replaceAll(systemProfile);
				log.fine(flowParam);
			}
			if (flowParam.contains("{$dynaTrace}")) {
				log.fine("*****dynaTrace Found!*****");
				Pattern p = Pattern.compile("(\\Q{$dynaTrace}\\E)");
				Matcher matches = p.matcher(flowParam);
				flowParam = matches.replaceAll(dynaTraceServer);
				log.fine(flowParam);
			}
			if (flowParam.contains("{$instance}")) {
				log.fine("*****Instance Found!*****");
				Pattern p = Pattern.compile("(\\Q{$instance}\\E)");
				Matcher matches = p.matcher(flowParam);
				flowParam = matches.replaceAll(incidentInstance);
				log.fine(flowParam);
			}
			if (flowParam.contains("{$instanceType}")) {
				log.fine("*****Instance Type Found!*****");
				Pattern p = Pattern.compile("(\\Q{$instanceType}\\E)");
				Matcher matches = p.matcher(flowParam);
				flowParam = matches.replaceAll(incidentInstanceType);
				log.fine(flowParam);
			}
			if (flowParam.contains("{$measureValue}")) {
				log.fine("*****Measure Value Found!*****");
				Pattern p = Pattern.compile("(\\Q{$measureValue}\\E)");
				Matcher matches = p.matcher(flowParam);
				flowParam = matches.replaceAll(measureValue);
				log.fine(flowParam);
			}
			if (flowParam.contains("{$measureUnit}")) {
				log.fine("*****Measure Unit Found!*****");
				Pattern p = Pattern.compile("(\\Q{$measureUnit}\\E)");
				Matcher matches = p.matcher(flowParam);
				flowParam = matches.replaceAll(measureUnit);
				log.fine(flowParam);
			}
			if (flowParam.contains("{$thresholdValue}")) {
				log.fine("*****Threshold Value Found!*****");
				Pattern p = Pattern.compile("(\\Q{$thresholdValue}\\E)");
				Matcher matches = p.matcher(flowParam);
				flowParam = matches.replaceAll(thresholdValue);
				log.fine(flowParam);
			}
			if (flowParam.contains("{$incidentMessage}")) {
				log.fine("*****Threshold Value Found!*****");
				Pattern p = Pattern.compile("(\\Q{$incidentMessage}\\E)");
				Matcher matches = p.matcher(flowParam);
				flowParam = matches.replaceAll(incidentMessage);
				log.fine(flowParam);
			}
			// Add flow escaped parameters to web call
			String[] parameters = flowParam.split("\\r?\\n");
			for (int x = 0; x < parameters.length; x++) {
				String line = parameters[x];
				line = StringEscapeUtils.escapeHtml4(line);
				if (x != 0) {
					line = "&" + line;
				}
				URL += line;
			}
		}
		// run hpoo web call
		log.info("HPOO Final Address: " + URL);
		String hpooRun = URLdownload.getURLString(URL, UserName, Password);
		log.info("HPPOO Return: " + hpooRun);
		log.info("############################ End of HPOO Processing #############################");
		return new Status(Status.StatusCode.Success);
	}

	@Override
	public void teardown(ActionEnvironment env) throws Exception {
	}

	private String getSeverityAsString(Incident incident) {
		if (incident.getSeverity() != null) {
			switch (incident.getSeverity()) {
			case Error:
				return "Critical";
			case Informational:
				return "Information";
			case Warning:
				return "Warning";
			}
		}
		return "";
	}
}
