package com.pos.services.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.ws.WebServiceContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.pos.a.model.ClaimAssingnedInfo;
import com.pos.a.model.ClaimBOResponse;
import com.pos.a.model.ClaimDecision;
import com.pos.a.model.ClaimDetailResponse;
import com.pos.a.model.ClaimEditRequest;
import com.pos.a.model.ClaimHeader;
import com.pos.a.model.ClaimHistory;
import com.pos.a.model.ClaimInfoResponse;
import com.pos.a.model.ClaimLine;
import com.pos.a.model.ClaimLineInfo;
import com.pos.a.model.ClaimLockRequest;
import com.pos.a.model.ClaimMoreDetails;
import com.pos.a.model.ClaimReasonInfo;
import com.pos.a.model.ClaimRequest;
import com.pos.a.model.ClaimSearchModel;
import com.pos.a.model.ClaimStatus;
import com.pos.a.model.ClaimSummary;
import com.pos.a.model.ClaimSummaryResponse;
import com.pos.a.model.MLMatchClaimLine;
import com.pos.a.model.MLResults;
import com.pos.a.model.MLRunStats;
import com.pos.a.model.MatchingClaimLine;
import com.pos.a.model.PredictionResult;
import com.pos.a.model.PredictionResultsRequest;
import com.pos.a.model.PredictionResultsResponse;
import com.pos.a.model.PredictionStat;
import com.pos.a.model.PredictionStatResponse;
import com.pos.a.model.TrainingGroup;
import com.pos.a.model.TrainingGroupRequest;
import com.pos.a.model.TrainingGroupResponse;
import com.pos.a.model.UserModel;
import com.pos.a.model.UserPermissions;
import com.pos.login.model.LoginResponse;
import com.pos.services.intf.ClaimService;
import com.pos.services.jdbc.JDBClaimUtilMysql;

public class ClaimServiceImpl implements ClaimService {

	private static final Logger logger = LogManager.getLogger(ClaimServiceImpl.class);
	private String currentdate;
	/** The context. */
	@Resource
	public WebServiceContext webServiceContext;

	@Context
	private org.apache.cxf.jaxrs.ext.MessageContext rsMessageContext;

	public void setWebServiceContext(WebServiceContext webServiceContext) {
		this.webServiceContext = webServiceContext;
	}

	public ClaimServiceImpl() {

	}

	@Override
	public Response getClaimList(LoginResponse request) throws Exception {

		ClaimSummaryResponse response = new ClaimSummaryResponse();
		List<ClaimSummary> claimlist = new ArrayList<ClaimSummary>();

		try {
			response = JDBClaimUtilMysql.getTypesList(request);
			if (request.getClaimsassginedlist() != null && !request.getClaimsassginedlist().isEmpty())
				claimlist = JDBClaimUtilMysql.getClaimList(request.getClaimsassginedlist());
			response.setClaimsummaryList(claimlist);
			response.setInfo("Total Claims found =" + claimlist.size());
			response = JDBClaimUtilMysql.getAgeandAmountList(request, response);

		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response getSelectedClaimList(ClaimRequest request) throws Exception {
		ClaimSummaryResponse response = new ClaimSummaryResponse();
		List<ClaimSummary> claimlist = new ArrayList<ClaimSummary>();

		try {
			claimlist = JDBClaimUtilMysql.getSelectedClaimList(request);
			response.setClaimsummaryList(claimlist);
			response.setInfo("Total Claims found =" + claimlist.size());
		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response getClaimDetails(ClaimSummary request) throws Exception {
		List<ClaimHeader> claimdetaillist = new ArrayList<ClaimHeader>();
		ClaimDetailResponse response = new ClaimDetailResponse();
		try {
			claimdetaillist = JDBClaimUtilMysql.getClaimDetails(request);
			response.setClaimdetaillist(claimdetaillist);
			response.setInfo("Total Claims found =" + claimdetaillist.size());
		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response getSelectedClaimDetails(ClaimRequest request) throws Exception {
		List<ClaimHeader> claimdetaillist = new ArrayList<ClaimHeader>();
		ClaimDetailResponse response = new ClaimDetailResponse();
		try {
			claimdetaillist = JDBClaimUtilMysql.getSelectedClaimDetails(request);
			response.setClaimdetaillist(claimdetaillist);
			response.setInfo("Total Claims found =" + claimdetaillist.size());
		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response getClaimInfo(ClaimHeader request) throws Exception {
		List<ClaimLine> claimlinelist = new ArrayList<ClaimLine>();
		ClaimInfoResponse response = new ClaimInfoResponse();
		List<ClaimHistory> historylist = new ArrayList<ClaimHistory>();
		List<MatchingClaimLine> claimlinesmatchedlist = new ArrayList<MatchingClaimLine>();
		List<ClaimLineInfo> claimlineinfolist = new ArrayList<ClaimLineInfo>();
		double value = 0;
		try {
			response = JDBClaimUtilMysql.getClaimInfo(request);
			claimlineinfolist = JDBClaimUtilMysql.processClaimLine(request);
			response.setClaimlinesinfolist(claimlineinfolist);
			if (response.getClaimlinelist() != null) {
				for (int i = 0; i < response.getClaimlinelist().size(); i++) {

					if (response.getClaimlinelist().get(i).getFinalAmount() != null
							&& response.getClaimlinelist().get(i).getFinalAmount().trim().length() > 0
							&& response.getClaimlinelist().get(i).getRecordType().equalsIgnoreCase("input")) {
						value = value + Double.parseDouble(response.getClaimlinelist().get(i).getFinalAmount());

					} else
						response.getClaimlinelist().get(i).setFinalAmount("0");
				}
				DecimalFormat format = new DecimalFormat("##.00");

				response.getClaimheader().setFinalAmt(Double.parseDouble(format.format(value)));
//				if(!request.getClaimCurrentStatus().trim().equalsIgnoreCase("Released"))
//				JDBClaimUtilMysql.UpdateFinalAmount(response.getClaimheader());
			}
			historylist = JDBClaimUtilMysql.getHistory(request);
			//claimlinesmatchedlist = JDBClaimUtilMysql.getClaimsMatchedLines(request);
			response.setClaimlinesmatchedlist(claimlinesmatchedlist);
			response.setHistorylist(historylist);
			response.setInfo("Total Claims found =" + claimlinelist.size());
		} catch (Exception e) {
			e.printStackTrace();
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response getMatchSearch(ClaimSearchModel request) throws Exception {

		List<ClaimHeader> headerList = new ArrayList<ClaimHeader>();

		headerList = JDBClaimUtilMysql.searchClaimIds(request.getSearch(), request);

		ClaimDetailResponse headerResponse = new ClaimDetailResponse();

		headerResponse.setClaimdetaillist(headerList);

		return Response.ok().entity(headerResponse).status(200)
				.header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response getClaimInfoForEdit(ClaimHeader request) throws Exception {

		ClaimInfoResponse response = new ClaimInfoResponse();
		try {
			response = JDBClaimUtilMysql.getClaimInfo(request);

		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response editClaimLine(ClaimEditRequest request) throws Exception {

		ClaimInfoResponse response = new ClaimInfoResponse();
		List<ClaimHistory> historylist = new ArrayList<ClaimHistory>();
		List<ClaimLineInfo> claimlineinfolist = new ArrayList<ClaimLineInfo>();
		String status = null;
		double value = 0;
		try {
			status = JDBClaimUtilMysql.editClaimLine(request);
			response = JDBClaimUtilMysql.getClaimInfo(request.getClaimHeader());
			claimlineinfolist = JDBClaimUtilMysql.processClaimLine(request.getClaimHeader());
			response.setClaimlinesinfolist(claimlineinfolist);
			if (response.getClaimlinelist() != null) {
				for (int i = 0; i < response.getClaimlinelist().size(); i++) {

					if (response.getClaimlinelist().get(i).getFinalAmount() != null
							&& response.getClaimlinelist().get(i).getFinalAmount().trim().length() > 0
							&& response.getClaimlinelist().get(i).getRecordType().equalsIgnoreCase("input")) {
						value = value + Double.parseDouble(response.getClaimlinelist().get(i).getFinalAmount());

					} else
						response.getClaimlinelist().get(i).setFinalAmount("0");
				}
				DecimalFormat format = new DecimalFormat("##.00");

				response.getClaimheader().setFinalAmt(Double.parseDouble(format.format(value)));

			}
			historylist = JDBClaimUtilMysql.getHistory(request.getClaimHeader());
			response.setHistorylist(historylist);
			JDBClaimUtilMysql.UpdateFinalAmount(response.getClaimheader());
			response.setInfo(status);
		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response lockClaim(ClaimEditRequest request) throws Exception {
		ClaimInfoResponse response = new ClaimInfoResponse();
		try {
			response = JDBClaimUtilMysql.lockClaim(request);

		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response submitDecision(ClaimDecision request) throws Exception {
		System.out.println("************* in submitDecision");
		ClaimInfoResponse response = new ClaimInfoResponse();
		String clmID=request.getClaiminforeponse().getClaimheader().getClaimId();
		String error = null;
		String info = null;
		List<ClaimLineInfo> claimlineinfolist = new ArrayList<ClaimLineInfo>();
		List<ClaimHistory> historylist = new ArrayList<ClaimHistory>();
		List<MatchingClaimLine> claimlinesmatchedlist = new ArrayList<MatchingClaimLine>();
		double value1 = 0;
		try {
			response = JDBClaimUtilMysql.submitDecision(request);
			if (response.getError() != null && response.getError().trim().length() > 0)
				error = response.getError();
			else {
				info = response.getInfo();
				if(request.getClaimnavtype()!=null && request.getClaimnavtype().equalsIgnoreCase("myworkqueue"))
				{
					
					response = JDBClaimUtilMysql.getMyworkClaimInfo(request.getUser());
				}
				else{
					
					response = JDBClaimUtilMysql.getClaimInfo(request.getClaiminforeponse().getClaimheader());
					
				}
			}
			
			response.setInfo(info);
			response.setError(error);
			response.setInfo("Submitted the claim Successfully");
			claimlineinfolist = JDBClaimUtilMysql.processClaimLine(response.getClaimheader());
			historylist = JDBClaimUtilMysql.getHistory(response.getClaimheader());
			claimlinesmatchedlist = JDBClaimUtilMysql.getClaimsMatchedLines(response.getClaimheader());
			response.setClaimlinesmatchedlist(claimlinesmatchedlist);
			response.setHistorylist(historylist);
			response.setClaimlinesinfolist(claimlineinfolist);
			if (response.getClaimlinelist() != null) {
				for (int i = 0; i < response.getClaimlinelist().size(); i++) {

					if (response.getClaimlinelist().get(i).getFinalAmount() != null
							&& response.getClaimlinelist().get(i).getFinalAmount().trim().length() > 0
							&& response.getClaimlinelist().get(i).getRecordType().equalsIgnoreCase("input")) {
						value1 = value1 + Double.parseDouble(response.getClaimlinelist().get(i).getFinalAmount());

					} else
						response.getClaimlinelist().get(i).setFinalAmount("0");
				}
				DecimalFormat format = new DecimalFormat("##.00");

				response.getClaimheader().setFinalAmt(Double.parseDouble(format.format(value1)));

				if(JDBClaimUtilMysql.UpdateFinalAmount(response.getClaimheader()))
				{
					System.out.println("clmID:::::"+clmID);
					if (!request.getClaimDecisionType().equalsIgnoreCase("Audit")
						&& request.getDecision().equalsIgnoreCase("accept"))
					{
							JDBClaimUtilMysql.deleteClaim(clmID);
					}
				}
			}
			
			

		} catch (Exception e) {
			response.setError(e.getMessage());
			e.printStackTrace();
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response claimStatus(ClaimStatus request) throws Exception {
		ClaimSummaryResponse response = new ClaimSummaryResponse();
		List<ClaimSummary> claimlist = new ArrayList<ClaimSummary>();

		try {

			claimlist = JDBClaimUtilMysql.getClaimStatusList(request);
			response.setClaimsummaryList(claimlist);
			response.setInfo("Total Claims found =" + claimlist.size());
		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response sendToAudit(ClaimInfoResponse request) throws Exception {
		ClaimInfoResponse response = new ClaimInfoResponse();
		String value = null;
		List<ClaimLineInfo> claimlineinfolist = new ArrayList<ClaimLineInfo>();
		List<ClaimHistory> historylist = new ArrayList<ClaimHistory>();
		List<MatchingClaimLine> claimlinesmatchedlist = new ArrayList<MatchingClaimLine>();
		double value1 = 0;
		try {
			value = JDBClaimUtilMysql.checkIsAudit(request);
			// tempory status change method for submitting the claim removew
			// method and plugin sampling code
			JDBClaimUtilMysql.updateAuditStatus(request, value);
			if (!request.getInfo().equalsIgnoreCase("myworkqueue"))

				response = JDBClaimUtilMysql.getClaimInfo(request.getClaimheader());
			else
				response = JDBClaimUtilMysql.getMyworkClaimInfo(request.getModel());

			response.setInfo("Submitted the claim Successfully");

			claimlineinfolist = JDBClaimUtilMysql.processClaimLine(response.getClaimheader());
			response.setClaimlinesinfolist(claimlineinfolist);
			if (response.getClaimlinelist() != null) {
				for (int i = 0; i < response.getClaimlinelist().size(); i++) {

					if (response.getClaimlinelist().get(i).getFinalAmount() != null
							&& response.getClaimlinelist().get(i).getFinalAmount().trim().length() > 0
							&& response.getClaimlinelist().get(i).getRecordType().equalsIgnoreCase("input")) {
						value1 = value1 + Double.parseDouble(response.getClaimlinelist().get(i).getFinalAmount());

					} else
						response.getClaimlinelist().get(i).setFinalAmount("0");
				}

				DecimalFormat format = new DecimalFormat("##.00");

				response.getClaimheader().setFinalAmt(Double.parseDouble(format.format(value1)));

				JDBClaimUtilMysql.UpdateFinalAmount(response.getClaimheader());
			}
			historylist = JDBClaimUtilMysql.getHistory(response.getClaimheader());
			claimlinesmatchedlist = JDBClaimUtilMysql.getClaimsMatchedLines(response.getClaimheader());
			response.setClaimlinesmatchedlist(claimlinesmatchedlist);
			response.setHistorylist(historylist);

		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response acceptClaimLine(ClaimEditRequest request) throws Exception {
		ClaimInfoResponse response = new ClaimInfoResponse();
		String status = null;
		List<ClaimHistory> historylist = new ArrayList<ClaimHistory>();
		List<ClaimLineInfo> claimlineinfolist = new ArrayList<ClaimLineInfo>();
		double value = 0;
		try {
			status = JDBClaimUtilMysql.acceptClaimLine(request);
			response = JDBClaimUtilMysql.getClaimInfo(request.getClaimHeader());
			claimlineinfolist = JDBClaimUtilMysql.processClaimLine(request.getClaimHeader());
			response.setClaimlinesinfolist(claimlineinfolist);
			if (response.getClaimlinelist() != null) {
				for (int i = 0; i < response.getClaimlinelist().size(); i++) {

					if (response.getClaimlinelist().get(i).getFinalAmount() != null
							&& response.getClaimlinelist().get(i).getFinalAmount().trim().length() > 0
							&& response.getClaimlinelist().get(i).getRecordType().equalsIgnoreCase("input")) {
						value = value + Double.parseDouble(response.getClaimlinelist().get(i).getFinalAmount());

					} else
						response.getClaimlinelist().get(i).setFinalAmount("0");
				}

				DecimalFormat format = new DecimalFormat("##.00");

				response.getClaimheader().setFinalAmt(Double.parseDouble(format.format(value)));

				JDBClaimUtilMysql.UpdateFinalAmount(response.getClaimheader());
			}
			historylist = JDBClaimUtilMysql.getHistory(request.getClaimHeader());
			response.setHistorylist(historylist);

			response.setInfo(status);
		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response lockedClaimsList(ClaimLockRequest request) throws Exception {
		List<ClaimHeader> claimdetaillist = new ArrayList<ClaimHeader>();
		ClaimDetailResponse response = new ClaimDetailResponse();
		try {
			claimdetaillist = JDBClaimUtilMysql.lockedClaimsList(request);
			response.setClaimdetaillist(claimdetaillist);
			response.setInfo("Total Claims found =" + claimdetaillist.size());
		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response lockedClaimsCount(UserModel request) throws Exception {
		ClaimDetailResponse response = new ClaimDetailResponse();
		int count = 0;

		try {
			count = JDBClaimUtilMysql.lockedClaimsCount(request);
			response.setInfo(Integer.toString(count));
			response = JDBClaimUtilMysql.todayProcessed(request, response);

		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response unlockClaim(ClaimLockRequest request) throws Exception {
		System.out.println(new Gson().toJson(request));
		List<ClaimHeader> claimdetaillist = new ArrayList<ClaimHeader>();
		ClaimDetailResponse response = new ClaimDetailResponse();

		try {
			if (JDBClaimUtilMysql.unlockClaim(request)) {
				claimdetaillist = JDBClaimUtilMysql.lockedClaimsList(request);
				response.setClaimdetaillist(claimdetaillist);
				response.setInfo("Total Claims found =" + claimdetaillist.size());
			} else
				response.setError("Could Not Process the Request");
		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response predictionResults(PredictionResultsRequest request) throws Exception {
		PredictionResultsResponse response = new PredictionResultsResponse();
		List<MLResults> list = new ArrayList<MLResults>();
		try {

			list = JDBClaimUtilMysql.predictionResults(request);
			response.setMlresults(list);
			response.setInfo("Processed Successfully");

		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response mlTestAndTrainData(PredictionResultsRequest request) throws Exception {
		PredictionResultsResponse response = new PredictionResultsResponse();
		List<MLMatchClaimLine> list = new ArrayList<MLMatchClaimLine>();
		ClaimSummaryResponse resp = new ClaimSummaryResponse();
		try {
			resp = JDBClaimUtilMysql.getTypesList(request.getLoginresponse());
			list = JDBClaimUtilMysql.mlTestData(request);
			response.setMlresponse(list);
			response.setClaimsummaryresponse(resp);
			response.setInfo("Processed Successfully");

		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response predictionReviewSummary(PredictionResultsRequest request) throws Exception {
		PredictionResultsResponse response = new PredictionResultsResponse();
		try {

			response = JDBClaimUtilMysql.predictionReviewSummary(request);

		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response releaseAllClaims(ClaimSummary request) throws Exception {
		ClaimSummaryResponse response = new ClaimSummaryResponse();
		List<ClaimSummary> claimlist = new ArrayList<ClaimSummary>();

		try {
			if (!JDBClaimUtilMysql.releaseAllClaims(request)) {
				response.setError("Error While Processing the Request");
			}
			
			response = JDBClaimUtilMysql.getTypesList(request.getResponse());
			claimlist = JDBClaimUtilMysql.getClaimList(request.getResponse().getClaimsassginedlist());
			response.setClaimsummaryList(claimlist);

		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response getSeletcedPredictionList(ClaimRequest request) throws Exception {
		PredictionResultsResponse response = new PredictionResultsResponse();
		List<MLMatchClaimLine> list = new ArrayList<MLMatchClaimLine>();
		ClaimSummaryResponse resp = new ClaimSummaryResponse();
		try {
			resp = JDBClaimUtilMysql.getTypesList(request.getLoginresponse());
			list = JDBClaimUtilMysql.getSeletcedPredictionList(request);
			response.setMlresponse(list);
			response.setClaimsummaryresponse(resp);
			response.setInfo("Processed Successfully");

		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response reasonsList(UserModel request) throws Exception {
		ClaimReasonInfo response = new ClaimReasonInfo();
		List<String> reasonslist = new ArrayList<String>();
		try {

			reasonslist = JDBClaimUtilMysql.reasonsList();
			response.setReasonslist(reasonslist);
			response.setInfo("Processed Successfully");

		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();

	}

	@Override
	public Response getmyAssignedClaims(UserModel request) throws Exception {
		List<ClaimHeader> claimdetaillist = new ArrayList<ClaimHeader>();
		ClaimDetailResponse response = new ClaimDetailResponse();
		try {
			claimdetaillist = JDBClaimUtilMysql.getmyAssignedClaims(request);
			response.setClaimdetaillist(claimdetaillist);
			response.setInfo("Total Claims found =" + claimdetaillist.size());
		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response assignedClaimsInfo(UserModel request) throws Exception {
		ClaimAssingnedInfo response = new ClaimAssingnedInfo();
		List<UserPermissions> assignedInfolist = new ArrayList<UserPermissions>();
		try {
			assignedInfolist = JDBClaimUtilMysql.assignedClaimsInfo(request);
			response.setAssignedInfolist(assignedInfolist);
			response.setInfo("Total Claims found =" + assignedInfolist.size());
		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response getMoreClaimInfo(ClaimLine request) throws Exception {
		ClaimBOResponse response = new ClaimBOResponse();
		ClaimMoreDetails headerinfo = new ClaimMoreDetails();
		try {
			headerinfo = JDBClaimUtilMysql.getMoreClaimInfo(request);
			response.setHeaderinfo(headerinfo);
			response.setInfo("Processed Successfully");
		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response newPredictionResults(UserModel request) throws Exception {
		List<PredictionResult> predictionresultlist = new ArrayList<PredictionResult>();
		List<MLRunStats> mlrunlist = new ArrayList<MLRunStats>();
		PredictionResultsResponse response = new PredictionResultsResponse();
		try {
			predictionresultlist = JDBClaimUtilMysql.newPredictionResults();
			mlrunlist = JDBClaimUtilMysql.getMLRunStat();
			response.setPredictionresultlist(predictionresultlist);
			response.setMlrunsatslist(mlrunlist);
			response = JDBClaimUtilMysql.getBuildPredictionChart(response);
			response.setInfo("Processed Successfully");
		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response newPredictionResultDetails(PredictionResult request) throws Exception {
		List<MLResults> predictionresultlist = new ArrayList<MLResults>();
		PredictionResultsResponse response = new PredictionResultsResponse();
		try {
			predictionresultlist = JDBClaimUtilMysql.newPredictionResultDetails(request);
			response.setMlresults(predictionresultlist);
			response.setInfo("Processed Successfully");
		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response newPredictionInfo(MLResults request) throws Exception {

		PredictionResultsResponse response = new PredictionResultsResponse();
		try {
			response = JDBClaimUtilMysql.newPredictionInfo(request);

		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();

	}

	@Override
	public Response selectedNewPredcitionResults(MLRunStats request) throws Exception {
		List<PredictionResult> predictionresultlist = new ArrayList<PredictionResult>();
		List<MLRunStats> mlrunlist = new ArrayList<MLRunStats>();
		PredictionResultsResponse response = new PredictionResultsResponse();
		try {
			// predictionresultlist=JDBClaimUtilMysql.newPredictionResults();
			mlrunlist = JDBClaimUtilMysql.getMLRunStat();
			response.setPredictionresultlist(predictionresultlist);
			response.setMlrunsatslist(mlrunlist);
			response = JDBClaimUtilMysql.getBuildSelectedPredictionChart(response, request);
			response.setInfo("Processed Successfully");
		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response predcitionStat(LoginResponse request) throws Exception {
		List<PredictionStat> predictionstatlist = new ArrayList<PredictionStat>();
		PredictionStatResponse response = new PredictionStatResponse();
		try {
			predictionstatlist = JDBClaimUtilMysql.predcitionStat(request.getModel().getClientCd());
			response.setPredcitionstatlist(predictionstatlist);
			response.setInfo("Processed Successfully");
		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response submitPredcitionStat(PredictionStat request) throws Exception {
		List<PredictionStat> predictionstatlist = new ArrayList<PredictionStat>();
		PredictionStatResponse response = new PredictionStatResponse();
		try {
			if (JDBClaimUtilMysql.submitPredcitionStat(request))
				response.setInfo("Processed Successfully");
			else
				response.setError("Could Not Process The Request");

			predictionstatlist = JDBClaimUtilMysql.predcitionStat(request.getClientcd());
			response.setPredcitionstatlist(predictionstatlist);

		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response trainingGroups(TrainingGroupRequest request) throws Exception {
		TrainingGroupResponse response = new TrainingGroupResponse();
		List<TrainingGroup> trainingGroupsList = new ArrayList<TrainingGroup>();
		try {

			trainingGroupsList = JDBClaimUtilMysql.trainingGroups(request);
			response.setInfo("Processed Request SuccessFully");

			response.setTrainingGroupsList(trainingGroupsList);
		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();

	}

	@Override
	public Response similarTrainingGroups(TrainingGroup request) throws Exception {
		TrainingGroupResponse response = new TrainingGroupResponse();
		List<TrainingGroup> trainingGroupsList = new ArrayList<TrainingGroup>();
		try {

			trainingGroupsList = JDBClaimUtilMysql.similarTrainingGroups(request);
			response.setInfo("Processed Request SuccessFully");

			response.setTrainingGroupsList(trainingGroupsList);
		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response getDashSelectedClaimDetails(ClaimRequest request) throws Exception {
		List<ClaimHeader> claimdetaillist = new ArrayList<ClaimHeader>();
		ClaimDetailResponse response = new ClaimDetailResponse();
		try {
			claimdetaillist = JDBClaimUtilMysql.getDashSelectedClaimDetails(request);
			response.setClaimdetaillist(claimdetaillist);
			response.setInfo("Total Claims found =" + claimdetaillist.size());
		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response getnewmyAssignedClaims(UserModel request) throws Exception {
		List<ClaimLine> claimlinelist = new ArrayList<ClaimLine>();
		ClaimInfoResponse response = new ClaimInfoResponse();
		List<ClaimHistory> historylist = new ArrayList<ClaimHistory>();
		List<MatchingClaimLine> claimlinesmatchedlist = new ArrayList<MatchingClaimLine>();
		List<ClaimLineInfo> claimlineinfolist = new ArrayList<ClaimLineInfo>();
		double value = 0;
		try {
			response = JDBClaimUtilMysql.getMyworkClaimInfo(request);
			claimlineinfolist = JDBClaimUtilMysql.processClaimLine(response.getClaimheader());
			response.setClaimlinesinfolist(claimlineinfolist);
			if (response.getClaimlinelist() != null) {
				for (int i = 0; i < response.getClaimlinelist().size(); i++) {

					if (response.getClaimlinelist().get(i).getFinalAmount() != null
							&& response.getClaimlinelist().get(i).getFinalAmount().trim().length() > 0
							&& response.getClaimlinelist().get(i).getRecordType().equalsIgnoreCase("input")) {
						value = value + Double.parseDouble(response.getClaimlinelist().get(i).getFinalAmount());

					} else
						response.getClaimlinelist().get(i).setFinalAmount("0");
				}
				DecimalFormat format = new DecimalFormat("##.00");

				response.getClaimheader().setFinalAmt(Double.parseDouble(format.format(value)));

				JDBClaimUtilMysql.UpdateFinalAmount(response.getClaimheader());
			}
			historylist = JDBClaimUtilMysql.getHistory(response.getClaimheader());
			claimlinesmatchedlist = JDBClaimUtilMysql.getClaimsMatchedLines(response.getClaimheader());
			response.setClaimlinesmatchedlist(claimlinesmatchedlist);
			response.setHistorylist(historylist);
			response.setInfo("Total Claims found =" + claimlinelist.size());
			response.setInfo("Processed Successfully");
		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();

	}

	@Override
	public Response myProcessedClaims(LoginResponse request) throws Exception {
		ClaimDetailResponse response = new ClaimDetailResponse();
		List<ClaimHeader> headerlist = new ArrayList<ClaimHeader>();
		try {
			headerlist = JDBClaimUtilMysql.myProcessedClaims(request);
			response.setClaimdetaillist(headerlist);
			response.setInfo("Processed Successfully");
		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();

	}

}
