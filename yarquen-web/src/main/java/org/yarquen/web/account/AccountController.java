package org.yarquen.web.account;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.yarquen.account.Account;
import org.yarquen.account.AccountService;
import org.yarquen.account.PasswordChange;
import org.yarquen.account.PasswordChangeRepository;
import org.yarquen.account.Role;
import org.yarquen.account.RoleService;
import org.yarquen.article.Article;
import org.yarquen.article.ArticleRepository;
import org.yarquen.category.CategoryBranch;
import org.yarquen.category.CategoryService;
import org.yarquen.trust.Trust;
import org.yarquen.validation.BeanValidationException;
import org.yarquen.web.enricher.CategoryTreeBuilder;
import org.yarquen.web.enricher.EnrichmentRecord;
import org.yarquen.web.enricher.EnrichmentRecordRepository;

@Controller
@RequestMapping("/account")
public class AccountController {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AccountController.class);

	// Minutes to expiring the unique url for password reset
	private static final int EXPIRATION_MINUTES = 15;

	@Resource
	private ArticleRepository articleRepository;
	@Resource
	private AccountService accountService;
	@Resource
	private EnrichmentRecordRepository enrichmentRecordRepository;
	@Resource
	private RoleService roleService;
	@Resource
	private PasswordChangeRepository passwordChangeRepository;
	@Resource
	private CategoryTreeBuilder categoryTreeBuilder;
	@Resource(name = "mail")
	private ResourceBundleMessageSource configMsg;

	@Resource
	private CategoryService categoryService;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(CategoryBranch.class,
				new PropertyEditorSupport() {
					@Override
					public void setAsText(String branch) {
						try {
							LOGGER.trace(
									"converting {} to a CategoryBranch object",
									branch);
							final CategoryBranch categoryBranch = new CategoryBranch();
							final StringTokenizer tokenizer = new StringTokenizer(
									branch, ".");
							while (tokenizer.hasMoreTokens()) {
								final String code = tokenizer.nextToken();
								categoryBranch.addSubCategory(code, null);
							}
							// fill names
							categoryService
									.completeCategoryBranchNodeNames(categoryBranch);
							setValue(categoryBranch);
						} catch (RuntimeException e) {
							LOGGER.error(":(", e);
							throw e;
						}
					}
				});
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String list(Model model) {
		LOGGER.debug("retrieval all account");
		Iterable<Account> accountList = accountService.findAll();
		model.addAttribute("accountList", accountList);
		return "account/list";

	}

	@RequestMapping(method = RequestMethod.POST)
	public String save(@Valid Account account, BindingResult result, Model model) {

		LOGGER.debug("saving user: {} \n {}", account, result);

		if (result.hasErrors()) {
			LOGGER.trace("errors!: {}", result.getAllErrors());
			return "account/register";
		}

		try {
			accountService.register(account);
		} catch (BeanValidationException e) {
			ObjectError error = new ObjectError("account", e.getMessage());
			result.addError(error);
			LOGGER.trace("errors!: {}", result.getAllErrors());
			return "account/register";
		}

		model.addAttribute("message", "account created successfully");
		return "message";
	}

	@RequestMapping(value = "/forgotPassword", params = "email", method = RequestMethod.GET)
	public String forgotPassword(
			@RequestParam(value = "email", required = true) String email,
			Model model) {
		final String baseUrl = configMsg.getMessage("baseUrl", null, null);
		LOGGER.debug("sending email to: [{}]", email);
		accountService.resetPasswordRequest(email, baseUrl
				+ "/account/passwordReset/");
		model.addAttribute("message",
				"Verification email sent successfully. Check your inbox.");
		return "message";
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public String update(@Valid Account account, BindingResult result,
			Model model) {

		LOGGER.debug("updating user: {} \n {}", account, result);

		if (result.hasErrors()) {
			LOGGER.trace("errors!: {}", result.getAllErrors());
			return "account/edit";
		}

		try {
			accountService.updateBasicInfo(account);
		} catch (BeanValidationException e) {
			ObjectError error = new ObjectError("account", e.getMessage());
			result.addError(error);
			LOGGER.trace("errors!: {}", result.getAllErrors());
			return "account/edit";
		}
		model.addAttribute("message", "account updated successfully");
		return "message";
	}

	@RequestMapping("register.html")
	public String register(Account newAccount, Model model) {
		model.addAttribute("account", newAccount);
		return "account/register";
	}

	@RequestMapping("forgotPassword.html")
	public String passwordChangeRequest() {
		return "account/forgotPassword";
	}

	@RequestMapping("current.html")
	public String showCurrent(Model model) {
		Account userDetails = (Account) SecurityContextHolder.getContext()
				.getAuthentication().getDetails();
		LOGGER.debug("userDetail: {}", userDetails);
		Account account = accountService.findOne(userDetails.getId());
		model.addAttribute("account", account);
		model.addAttribute("currentAccount", userDetails.getId());
		
		//enrichment history
		
		final List<EnrichmentRecord> articleHistory = enrichmentRecordRepository
				.findByAccountId(account.getId(), new Sort(new Sort.Order(
						Sort.Direction.DESC, "versionDate")));
		final Map<EnrichmentRecord, Article> historyWrapper = new LinkedHashMap<EnrichmentRecord, Article>();

		for (EnrichmentRecord enrichmentRecord : articleHistory) {
			final Article article = articleRepository
					.findOne(enrichmentRecord.getArticleId());
			historyWrapper.put(enrichmentRecord, article);
		}
		if (articleHistory != null && !articleHistory.isEmpty()) {
			model.addAttribute("articleRecords", historyWrapper);
		}

		LOGGER.debug("Account ID: [{}] has {} enrichment records.",
				account.getId(), articleHistory.size());
		
		Trust trustAction = new Trust();
		Node user = trustAction.getNode(account.getId());
		
		List<String> idTrustees = trustAction.getTrustees(account.getId());
		
		Map<Account, Double> accountWrapperDirect = new LinkedHashMap<Account, Double>();
		Map<Account, Double> accountWrapperInferred = new LinkedHashMap<Account, Double>();
		for(String accounts : idTrustees){
			Node sink = trustAction.getNode(accounts);
			if(trustAction.checkAdjacency(user, sink)){
				accountWrapperDirect.put(accountService.findOne(accounts),trustAction.getTrust(user, sink));
			}
			else
				accountWrapperInferred.put(accountService.findOne(accounts),trustAction.getTrust(user, sink));

		}
		
		if (idTrustees != null && !idTrustees.isEmpty()) {
			model.addAttribute("trusteesDirect", accountWrapperDirect);
			model.addAttribute("trusteesInferred", accountWrapperInferred);
		}
		
		Map<Account, Double> accountWrapperDirectTrusters = new LinkedHashMap<Account, Double>();
		Map<Account, Double> accountWrapperInferredTrusters = new LinkedHashMap<Account, Double>();

		List<String> idTrusters = trustAction.getTrusters(account.getId());

		for(String accounts : idTrusters){
			Node sink = trustAction.getNode(accounts);
			if(trustAction.checkAdjacency(sink,user)){
				accountWrapperDirectTrusters.put(accountService.findOne(accounts),trustAction.getTrust(sink,user));
			}
			else
				accountWrapperInferredTrusters.put(accountService.findOne(accounts),trustAction.getTrust(sink,user));

		}
		
		if (idTrusters != null && !idTrusters.isEmpty()) {
			model.addAttribute("trustersDirect", accountWrapperDirectTrusters);
			model.addAttribute("trustersInferred", accountWrapperInferredTrusters);
		}
		
		return "account/show";
	}

	
	@RequestMapping("/show/{accountId}")
	public String showAccount(@PathVariable("accountId") String accountId,
			Model model) {
		Account userDetails = (Account) SecurityContextHolder.getContext()
				.getAuthentication().getDetails();
		
		Account account = accountService.findOne(accountId);
		LOGGER.debug("userDetail: {}", accountId);
		model.addAttribute("account", account);
		model.addAttribute("currentAccount", userDetails.getId());
		
		//enrichment history
		
		final List<EnrichmentRecord> articleHistory = enrichmentRecordRepository
				.findByAccountId(account.getId(), new Sort(new Sort.Order(
						Sort.Direction.DESC, "versionDate")));
		final Map<EnrichmentRecord, Article> historyWrapper = new LinkedHashMap<EnrichmentRecord, Article>();

		for (EnrichmentRecord enrichmentRecord : articleHistory) {
			final Article article = articleRepository
					.findOne(enrichmentRecord.getArticleId());
			historyWrapper.put(enrichmentRecord, article);
		}
		if (articleHistory != null && !articleHistory.isEmpty()) {
			model.addAttribute("articleRecords", historyWrapper);
		}

		LOGGER.debug("Account ID: [{}] has {} enrichment records.",
				account.getId(), articleHistory.size());
		
		Trust trustAction = new Trust();
		Node user = trustAction.getNode(account.getId());
		
		List<String> idTrustees = trustAction.getTrustees(account.getId());
		
		Map<Account, Double> accountWrapperDirect = new LinkedHashMap<Account, Double>();
		Map<Account, Double> accountWrapperInferred = new LinkedHashMap<Account, Double>();
		for(String accounts : idTrustees){
			Node sink = trustAction.getNode(accounts);
			if(trustAction.checkAdjacency(user, sink)){
				accountWrapperDirect.put(accountService.findOne(accounts),trustAction.getTrust(user, sink));
			}
			else
				accountWrapperInferred.put(accountService.findOne(accounts),trustAction.getTrust(user, sink));

		}
		
		if (idTrustees != null && !idTrustees.isEmpty()) {
			model.addAttribute("trusteesDirect", accountWrapperDirect);
			model.addAttribute("trusteesInferred", accountWrapperInferred);
		}
		
		Map<Account, Double> accountWrapperDirectTrusters = new LinkedHashMap<Account, Double>();
		Map<Account, Double> accountWrapperInferredTrusters = new LinkedHashMap<Account, Double>();

		List<String> idTrusters = trustAction.getTrusters(account.getId());

		for(String accounts : idTrusters){
			Node sink = trustAction.getNode(accounts);
			if(trustAction.checkAdjacency(sink,user)){
				accountWrapperDirectTrusters.put(accountService.findOne(accounts),trustAction.getTrust(sink,user));
			}
			else
				accountWrapperInferredTrusters.put(accountService.findOne(accounts),trustAction.getTrust(sink,user));

		}
		
		if (idTrusters != null && !idTrusters.isEmpty()) {
			model.addAttribute("trustersDirect", accountWrapperDirectTrusters);
			model.addAttribute("trustersInferred", accountWrapperInferredTrusters);
		}
		
		Node me = trustAction.getNode(userDetails.getId());
		if(trustAction.checkAdjacency(me,user)){
			model.addAttribute("trustValue",trustAction.getAdjacencyTrust(me,user));
			model.addAttribute("knows",true);
		}
		else{
			model.addAttribute("trustValue","5");
		}

		
		model.addAttribute("user",accountId);
		model.addAttribute("trust",true);

		
		return "account/show";
	}
	

	@RequestMapping(value="/addTrust",method = RequestMethod.GET)
	public void addTrust(
			@RequestParam("user") String user,
			@RequestParam("trust") String trust, HttpServletResponse rsp) 
					throws IOException {
		boolean op;
		LOGGER.debug("Setting trust");
		Account userDetails = (Account) SecurityContextHolder.getContext()
				.getAuthentication().getDetails();
		
		Trust trustAction = new Trust();
		Node source = trustAction.getNode(userDetails.getId());
		Node sink = trustAction.getNode(user);

		op = trustAction.setTrust(Integer.parseInt(trust), source, sink);
		
		if(op){
			LOGGER.info("The user {} was trusted by {} sucessfully",user,userDetails.getId());
			rsp.setStatus(HttpServletResponse.SC_OK);
			rsp.getWriter().print("TRANSACTION_OK");
		}
		else{
			LOGGER.error("Error adding trust");
			rsp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			rsp.getWriter().print("TRANSACTION_ERROR");
		}
				
	}
	
	@RequestMapping(value="/addTrustTreshold",method = RequestMethod.GET)
	public void addTrustTreshold(
			@RequestParam("user") String user,
			@RequestParam("trust") String trust, HttpServletResponse rsp) 
					throws IOException {
		boolean op;
		LOGGER.debug("Setting trust treshold");

		op = trustAction.setTrust(Integer.parseInt(trust), source, sink);
		
		if(op){
			LOGGER.info("The user {} was trusted by {} sucessfully",user,userDetails.getId());
			rsp.setStatus(HttpServletResponse.SC_OK);
			rsp.getWriter().print("TRANSACTION_OK");
		}
		else{
			LOGGER.error("Error adding trust");
			rsp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			rsp.getWriter().print("TRANSACTION_ERROR");
		}
				
	}
	
	
	
	
	@RequestMapping(value="/deleteTrust/{user}",method = RequestMethod.GET)
	public ModelAndView deleteTrust(
			@PathVariable("user") String user,
		    HttpServletResponse rsp, Model model) 
					throws IOException {
		boolean op;
		LOGGER.debug("Deleting trust");
		Account userDetails = (Account) SecurityContextHolder.getContext()
				.getAuthentication().getDetails();
		
		Trust trustAction = new Trust();
		Node source = trustAction.getNode(userDetails.getId());
		Node sink = trustAction.getNode(user);

		op = trustAction.deleteRelationship(source, sink);

		if(op){
			LOGGER.info("The relationship {} was deleted sucessfully",user);
			 return new ModelAndView("redirect:" + "/account/current.html");
		}
		else{
			LOGGER.error("Error deleting trust");
			rsp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			rsp.getWriter().print("TRANSACTION_ERROR");
			return new ModelAndView("redirect:" + "error.html");
		}
				
	}
	
	

	@RequestMapping(value = "/edit/{accountId}", method = RequestMethod.GET)
	public String edit(@PathVariable("accountId") String accountId, Model model) {
		LOGGER.debug("accountId to edit: {}", accountId);

		Account account = accountService.findOne(accountId);

		if (account != null) {
			Iterable<Role> roles = roleService.findAll();
			model.addAttribute("roles", roles);
			model.addAttribute("account", account);
			return "account/edit";
		} else
			return "error";
	}

	@RequestMapping(value = "/passwordReset/{token}", method = RequestMethod.GET)
	public String passwordReset(@PathVariable("token") String token, Model model) {
		LOGGER.debug("password reset request from token: {}", token);
		final PasswordChange passwordChange = passwordChangeRepository
				.findByToken(token);

		// Expired Time for unique link request 15 minutes
		final Calendar expired = Calendar.getInstance(TimeZone
				.getTimeZone("UTC"));
		expired.add(Calendar.MINUTE, -EXPIRATION_MINUTES);
		final Date expiredDate = expired.getTime();
		LOGGER.debug("request date: {} - expiration date: {}",
				passwordChange.getRequestDate(), expiredDate);
		if (passwordChange != null
				&& passwordChange.getRequestDate().after(expiredDate)) {
			model.addAttribute("account", passwordChange.getAccount());

			return "account/passwordChange";
		} else {
			LOGGER.error("password change link expired");
			model.addAttribute("message",
					"the link for your password change has expired.");
			return "message";
		}
	}

	@RequestMapping(value = "/passwordChange", method = RequestMethod.POST)
	public String passwordChange(@Valid Account account, BindingResult result,
			Model model) {
		LOGGER.debug("password change for account: {}", account.getUsername());
		try {
			accountService.updatePassword(account);
		} catch (BeanValidationException e) {
			ObjectError error = new ObjectError("account", e.getMessage());
			result.addError(error);
			LOGGER.trace("errors!: {}", result.getAllErrors());
			return "account/passwordChange";
		}
		model.addAttribute("message",
				"your password has been updated, please login.");
		return "message";
	}

	@RequestMapping(value = "/passwordChange/{accountId}", method = RequestMethod.GET)
	public String setupPasswordChange(
			@PathVariable("accountId") String accountId, Model model) {
		Account account = accountService.findOne(accountId);
		LOGGER.debug("changing password for account: {}", account.getUsername());
		model.addAttribute("account", account);
		return "account/passwordChange";

	}

	@RequestMapping(value = "/setupSkills/{accountId}", method = RequestMethod.GET)
	public String setupSkills(@PathVariable("accountId") String accountId,
			Model model) {
		LOGGER.debug("setuping skills for accountId {}", accountId);
		Account account = accountService.findOne(accountId);
		if (account != null) {
			// categories
			final List<Map<String, Object>> categoryTree = categoryTreeBuilder
					.buildTree();
			model.addAttribute("categories", categoryTree);
			model.addAttribute("account", account);
			return "account/editSkills";
		} else
			return "error";

	}

	@RequestMapping(value = "/skills/", method = RequestMethod.POST)
	public String updateSkills(@Valid Account account, BindingResult result,
			Model model) {
		LOGGER.debug("skills for accountId {} : {}", account.getId(),
				account.getSkills());
		final List<Map<String, Object>> categoryTree = categoryTreeBuilder
				.buildTree();
		model.addAttribute("categories", categoryTree);
		model.addAttribute("account", account);
		if (result.hasErrors()) {
			LOGGER.trace("errors!: {}", result.getAllErrors());
			return "account/editSkills";
		}
		try {
			Account accountWithoutSkill = accountService.findOne(account
					.getId());
			accountWithoutSkill.setSkills(account.getSkills());
			accountService.updateSkills(accountWithoutSkill);
		} catch (BeanValidationException e) {
			ObjectError error = new ObjectError("account", e.getMessage());
			result.addError(error);
			LOGGER.trace("errors!: {}", result.getAllErrors());
			return "account/editSkills";
		}
		model.addAttribute("account", account);
		return "account/show";

	}
}


