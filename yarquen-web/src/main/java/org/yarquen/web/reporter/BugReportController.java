package org.yarquen.web.reporter;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.yarquen.account.Account;

/**
 * Controller for report and suggestions
 * 
 * @author Choon-ho Yoon
 * @date Apr 16, 2013
 * @version
 * 
 */
@Controller
@RequestMapping("/report")
public class BugReportController {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(BugReportController.class);

	@Resource
	private BugReportRepository bugReportRepository;

	@RequestMapping(method = RequestMethod.GET)
	public String setupFormSubmit(Model model, HttpServletRequest request) {
		LOGGER.debug("Setup of report and suggestions form.");
		final BugReport bugReport = new BugReport();

		Account userDetails = (Account) SecurityContextHolder.getContext()
				.getAuthentication().getDetails();
		bugReport.setAuthor(userDetails.getFirstName() + " "
				+ userDetails.getFamilyName());

		model.addAttribute("bugReport", bugReport);

		return "report/submitReport";
	}

	@RequestMapping(value = "/submit/", method = RequestMethod.POST)
	public String saveReport(@ModelAttribute("bugReport") BugReport bugReport,
			BindingResult result, Model model, RedirectAttributes redirAtts) {

		if (result.hasErrors()) {
			LOGGER.error("errors!: {}", result.getAllErrors());
			return "report/submitReport";
		} else {
			bugReport.setReportDate(Calendar.getInstance(
					TimeZone.getTimeZone("UTC")).getTime());
			bugReportRepository.save(bugReport);
			model.addAttribute("message", "thank you for your feedback.");
			return "message";
		}

	}

	@RequestMapping(value = "/viewReports", method = RequestMethod.GET)
	public String viewReports(Model model, HttpServletRequest request) {
		final List<BugReport> reports = bugReportRepository.findAll(new Sort(
				new Sort.Order(Sort.Direction.DESC, "reportDate")));
		model.addAttribute("reports", reports);
		LOGGER.debug("[{}] reports found", reports.size());
		return "report/viewReports";
	}

	@RequestMapping(value = "/reportDetail/{reportId}", method = RequestMethod.GET)
	public String viewReportDetail(@PathVariable("reportId") String reportId,
			Model model, HttpServletRequest request) {

		final BugReport bugReport = bugReportRepository.findOne(reportId);
		if (bugReport != null) {
			model.addAttribute("bugReport", bugReport);
			return "report/reportDetail";
		} else {
			throw new RuntimeException("The report was not found.");
		}

	}
}
