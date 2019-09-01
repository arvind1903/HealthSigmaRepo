package com.pos.services.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
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
import com.pos.a.model.ClaimRequest;
import com.pos.a.model.ClaimSearchModel;
import com.pos.a.model.ClaimStatus;
import com.pos.a.model.ClaimSummary;
import com.pos.a.model.ClaimSummaryResponse;
import com.pos.a.model.MLFeatureRecord;
import com.pos.a.model.MLMatchClaimLine;
import com.pos.a.model.MLResults;
import com.pos.a.model.MLRunStats;
import com.pos.a.model.MatchingClaimLine;
import com.pos.a.model.PredictionAccuracy;
import com.pos.a.model.PredictionResult;
import com.pos.a.model.PredictionResultsRequest;
import com.pos.a.model.PredictionResultsResponse;
import com.pos.a.model.PredictionStat;
import com.pos.a.model.TrainingGroup;
import com.pos.a.model.TrainingGroupRequest;
import com.pos.a.model.UserModel;
import com.pos.a.model.UserPermissions;
import com.pos.login.model.LoginResponse;
import com.pos.login.model.UserAuthorizedWorkQueue;
import com.pos.services.util.ConnectionUtil;

public class JDBClaimUtilMysql {

	private static final Log logger = LogFactory.getLog(JDBClaimUtilMysql.class.getName());
	private static Gson gson = new Gson();
	
	
	private static List<ClaimSummary> getInProcessClaimSummary(String workQueue) throws Exception{
		List<ClaimSummary> claimlist = new ArrayList<ClaimSummary>();
		ClaimSummary claim = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionUtil.getConnection();
			String query = "SELECT a.health_plan,a.employer_group,a.product,SUM(a.MLPrepCount) AS PrepForML,"
					+ " SUM(a.ReviewCount) AS HoldForReview, SUM(a.AuditCount) AS HoldForAudit"
					+ " ,SUM(a.ForReleaseCount) AS HoldForRelease,SUM(a.ReleasedCount) AS Released"
					+ " FROM (SELECT health_plan,employer_group,product,CASE claim_status WHEN 'Prep For ML' THEN COUNT(*)"
					+ " ELSE 0 END AS MLPrepCount,CASE claim_status WHEN 'Hold For Review' THEN COUNT(*) ELSE 0 END AS ReviewCount,"
					+ " CASE claim_status WHEN 'Hold For Audit' THEN COUNT(*) ELSE 0 END AS AuditCount,"
					+ " CASE claim_status WHEN 'Hold For Release' THEN COUNT(*) ELSE 0 END AS ForReleaseCount,"
					+ " CASE claim_status WHEN 'Released' THEN COUNT(*) ELSE 0 END AS ReleasedCount FROM hs_claim_header where ";
			query = query + workQueue;
		
			query = query
					+ " GROUP BY health_plan,employer_group,product,claim_status) a GROUP BY a.health_plan,a.employer_group,a.product";
			System.out.println("query" + query);
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				claim = new ClaimSummary();
				claim.setHealthplan(rs.getString("health_plan"));
				claim.setProduct(rs.getString("product"));
				claim.setEmployergroup(rs.getString("employer_group"));
				claim.setPrepcount(rs.getString("PrepForML"));
				claim.setReviewCount(rs.getString("HoldForReview"));
				claim.setReleaseCount(rs.getString("HoldForRelease"));
				claim.setReleasedCount(rs.getString("Released"));
				claim.setAuditCount(rs.getString("HoldForAudit"));
				int count = rs.getInt("PrepForML") + rs.getInt("HoldForReview") + rs.getInt("HoldForRelease")
						+ rs.getInt("Released") + rs.getInt("HoldForAudit");
				claim.setRecievedCount(Integer.toString(count));
				claimlist.add(claim);
			}
	
	} catch (Exception ex) {
		ex.printStackTrace();
	} finally {
		if (conn != null)
			conn.close();
		if (pstmt != null)
			pstmt.close();
	}
		
		return claimlist ;
	}
	
	
	private static List<ClaimSummary> getReleasedClaimSummary(String workQueue) throws Exception{
		List<ClaimSummary> claimlist = new ArrayList<ClaimSummary>();
		ClaimSummary claim = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionUtil.getConnection();
			String query = "SELECT a.health_plan,a.employer_group,a.product,SUM(a.ReleasedCount) AS Released"
					+ " FROM (SELECT health_plan,employer_group,product,COUNT(*) AS ReleasedCount FROM hs_claim_header_released where ";
			query = query + workQueue;
			query = query
					+ " GROUP BY health_plan,employer_group,product,claim_status) a GROUP BY a.health_plan,a.employer_group,a.product";
			System.out.println("query" + query);
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				claim = new ClaimSummary();
				claim.setHealthplan(rs.getString("health_plan"));
				claim.setProduct(rs.getString("product"));
				claim.setEmployergroup(rs.getString("employer_group"));
				claim.setReleasedCount(rs.getString("Released"));
				claimlist.add(claim);
			}
	
	} catch (Exception ex) {
		ex.printStackTrace();
	} finally {
		if (conn != null)
			conn.close();
		if (pstmt != null)
			pstmt.close();
	}
		
		return claimlist ;
	}
	
	private static List<ClaimSummary> mergeClaimSummary(List<ClaimSummary> inProcessClaimSummary, List<ClaimSummary> releasedClaimSummary) throws Exception{
		List<ClaimSummary> claimSummary = new ArrayList<ClaimSummary>();
		/*
		 * iterate over the two lists and merge the released claims with in process claims
		 * Step1 : Iterate over inprocess list and get details from released list when exists
		 * Step2 : Iterate over released list if not present in inprocess list then add the row, this is needed to address
		 * when all the claims are released and there is no pending  
		 */
		for(ClaimSummary inProcessRow:inProcessClaimSummary){
			String  releasedClaimsExist = "NO";
			
			for(ClaimSummary releasedRow:releasedClaimSummary){
				if(inProcessRow.getProduct().equalsIgnoreCase(releasedRow.getProduct()) &&
						inProcessRow.getHealthplan().equalsIgnoreCase(releasedRow.getHealthplan()) &&
						inProcessRow.getEmployergroup().equalsIgnoreCase(releasedRow.getEmployergroup())
						){
								if(releasedRow.getReleasedCount()!=null) { 
										inProcessRow.setReleasedCount(releasedRow.getReleasedCount());
							    }
								int receivedCount = 0;
								if (inProcessRow.getReviewCount()!= null) {
									receivedCount = receivedCount + Integer.parseInt(inProcessRow.getReviewCount());
								}
								
								if (inProcessRow.getAuditCount()!=null){
									receivedCount = receivedCount + Integer.parseInt(inProcessRow.getAuditCount());
								}
								
								if(inProcessRow.getReleaseCount()!=null){
									receivedCount = receivedCount+ Integer.parseInt(inProcessRow.getReleaseCount());
								}
								
								if(inProcessRow.getPrepcount()!=null){
									receivedCount = receivedCount + Integer.parseInt(inProcessRow.getPrepcount());
								}
								
								if(releasedRow.getReleasedCount()!=null){
									receivedCount = receivedCount+ Integer.parseInt(releasedRow.getReleasedCount());
								}
					inProcessRow.setRecievedCount(receivedCount+"");
					claimSummary.add(inProcessRow);
					releasedClaimsExist = "YES";
				}
			}
			if(releasedClaimsExist.equalsIgnoreCase("NO")){
				claimSummary.add(inProcessRow);
			}
		}
		
		/*
		 * This is to ensure that all released claims which does not have any in process claims are also added to the list
		 */
		for(ClaimSummary releasedRow:releasedClaimSummary){
			String  claimRowExists = "NO";
				for(ClaimSummary summaryRow:claimSummary){
					if(releasedRow.getProduct().equalsIgnoreCase(summaryRow.getProduct()) &&
							releasedRow.getHealthplan().equalsIgnoreCase(summaryRow.getHealthplan()) &&
							releasedRow.getEmployergroup().equalsIgnoreCase(summaryRow.getEmployergroup())
							){
						claimRowExists ="YES" ;
					}
			}
				if(claimRowExists.equalsIgnoreCase("NO")){
					releasedRow.setRecievedCount(releasedRow.getReleasedCount());
					claimSummary.add(releasedRow);
				}
		}
		
		
		return claimSummary;
	}

	public static List<ClaimSummary> getClaimList(List<UserAuthorizedWorkQueue> userAuthorizedWorkQueueList) throws Exception {
		List<ClaimSummary> inProcessClaimSummary = new ArrayList<ClaimSummary>();
		List<ClaimSummary> releasedClaimSummary = new ArrayList<ClaimSummary>();
		List<ClaimSummary> claimSummary = new ArrayList<ClaimSummary>();
		String queue = "";
		try {
			if (userAuthorizedWorkQueueList != null) {

				for (int i = 0; i < userAuthorizedWorkQueueList.size(); i++) {
					if (i == 0)
						queue = queue + "(";
					else
						queue = queue + " OR (";
					if (userAuthorizedWorkQueueList.get(i).getHealthplan() != null) {
						queue = queue + " health_plan='" + userAuthorizedWorkQueueList.get(i).getHealthplan() + "'";
					}
					if (userAuthorizedWorkQueueList.get(i).getProduct() != null) {
						queue = queue + " and product='" + userAuthorizedWorkQueueList.get(i).getProduct() + "'";
					}
					if (userAuthorizedWorkQueueList.get(i).getEmpgrp() != null) {
						queue = queue + " and client_cd='" + userAuthorizedWorkQueueList.get(i).getClientCd() + "'";
					}
					if (userAuthorizedWorkQueueList.get(i).getEmpgrp() != null) {
						queue = queue + " and employer_group='" + userAuthorizedWorkQueueList.get(i).getEmpgrp() + "')";
					}

				}

			}

			System.out.println(" user work queue" + queue);
			
			inProcessClaimSummary = getInProcessClaimSummary(queue);
			releasedClaimSummary  = getReleasedClaimSummary(queue);
			claimSummary = mergeClaimSummary(inProcessClaimSummary,releasedClaimSummary);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return claimSummary;
	}
	
	
	
	

	public static ClaimSummaryResponse getTypesList(LoginResponse request) throws SQLException {
		ClaimSummaryResponse response = new ClaimSummaryResponse();
		Set<String> prodlist = new HashSet<String>();
		Set<String> emplist = new HashSet<String>();
		Set<String> healthlist = new HashSet<String>();
		Set<String> clientcdlist = new HashSet<String>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionUtil.getConnection();
			String query = "select  health_plan,product,employer_group,client_cd from hs_user_workqueue where userid='"
					+ request.getModel().getUserId() + "' and client_cd='" + request.getModel().getClientCd().trim()
					+ "'";
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				healthlist.add(rs.getString("health_plan").trim().toUpperCase());

				prodlist.add(rs.getString("product").trim().toUpperCase());

				emplist.add(rs.getString("employer_group").trim().toUpperCase());
				clientcdlist.add(rs.getString("client_cd").trim().toUpperCase());
			}

			response.setEmpgrpList(new ArrayList(emplist));
			response.setProductList(new ArrayList(prodlist));
			response.setHealthplanList(new ArrayList(healthlist));
			response.setClientCodeList(new ArrayList(clientcdlist));

		} catch (Exception ex) {
			response.setError("Error in getTypesList:: JDBClaimUtilMysql class");
			ex.printStackTrace();
		} finally {

			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				conn.close();
		}

		return response;
	}

	public static List<ClaimSummary> getSelectedClaimList(ClaimRequest request) throws SQLException {

		List<ClaimSummary> claimlist = new ArrayList<ClaimSummary>();
		ClaimSummary claim = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt2 = null;
		UserPermissions permission = new UserPermissions();
		List<UserPermissions> permissionlist = new ArrayList<UserPermissions>();
		ResultSet rs2 = null;
		ResultSet rs = null;
		int count = 0;
		boolean health = true;
		boolean product = true;
		boolean empgrp = true;
		String queue = "";
		try {

			conn = ConnectionUtil.getConnection();

			if (request.getHealthplanType().equalsIgnoreCase("All"))
				health = false;
			if (request.getProductType().equalsIgnoreCase("All"))
				product = false;
			if (request.getEmpgrpType().equalsIgnoreCase("All"))
				empgrp = false;

			String query2 = "select * from hs_user_workqueue where userid=? and active='Y'";
			if (health)
				query2 = query2 + " and health_plan='" + request.getHealthplanType() + "'";
			if (empgrp)
				query2 = query2 + " and employer_group='" + request.getEmpgrpType() + "'";
			if (product)
				query2 = query2 + " and product='" + request.getProductType() + "'";
		
			pstmt2 = conn.prepareStatement(query2);
			pstmt2.setInt(1, request.getLoginresponse().getModel().getUserId());
			rs2 = pstmt2.executeQuery();
			while (rs2.next()) {
				permission = new UserPermissions();
				permission.setEmpgrp(rs2.getString("Employer_Group"));
				permission.setHealthplan(rs2.getString("Health_Plan"));
				permission.setProduct(rs2.getString("Product"));
				permission.setStatus(rs2.getString("status"));
				permission.setClientCd(rs2.getString("client_cd"));
				permissionlist.add(permission);

			}
			if (permissionlist != null) {
				queue = "(";
				for (int i = 0; i < permissionlist.size(); i++) {
					if (i == 0)
						queue = queue + "(";
					else
						queue = queue + " OR (";
					if (permissionlist.get(i).getHealthplan() != null) {
						queue = queue + " health_plan='" + permissionlist.get(i).getHealthplan() + "'";
					}
					if (permissionlist.get(i).getProduct() != null) {
						queue = queue + " and product='" + permissionlist.get(i).getProduct() + "'";
					}
					if (permissionlist.get(i).getEmpgrp() != null) {
						queue = queue + " and client_cd='" + permissionlist.get(i).getClientCd() + "'";
					}
					if (permissionlist.get(i).getEmpgrp() != null) {
						queue = queue + " and employer_group='" + permissionlist.get(i).getEmpgrp() + "')";
					}

				}

			}
			if (queue.trim().length() > 2) {

				String query = "SELECT a.health_plan,a.employer_group,a.product,SUM(a.MLPrepCount) AS PrepForML,"
						+ " SUM(a.ReviewCount) AS HoldForReview, SUM(a.AuditCount) AS HoldForAudit"
						+ " ,SUM(a.ForReleaseCount) AS HoldForRelease,SUM(a.ReleasedCount) AS Released"
						+ " FROM (SELECT health_plan,employer_group,product,CASE claim_status WHEN 'Prep For ML' THEN COUNT(*)"
						+ " ELSE 0 END AS MLPrepCount,CASE claim_status WHEN 'Hold For Review' THEN COUNT(*) ELSE 0 END AS ReviewCount,"
						+ " CASE claim_status WHEN 'Hold For Audit' THEN COUNT(*) ELSE 0 END AS AuditCount,"
						+ " CASE claim_status WHEN 'Hold For Release' THEN COUNT(*) ELSE 0 END AS ForReleaseCount,"
						+ " CASE claim_status WHEN 'Released' THEN COUNT(*) ELSE 0 END AS ReleasedCount FROM hs_claim_header where ";
				query = query + queue + ")";

				query = query
						+ " GROUP BY health_plan,employer_group,product,claim_status) a GROUP BY a.health_plan,a.employer_group,a.product";
			
				System.out.println("QUERY" + query);
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					claim = new ClaimSummary();
					claim.setHealthplan(rs.getString("health_plan"));
					claim.setProduct(rs.getString("product"));
					claim.setEmployergroup(rs.getString("employer_group"));
					claim.setPrepcount(rs.getString("PrepForML"));
					claim.setReviewCount(rs.getString("HoldForReview"));
					claim.setReleaseCount(rs.getString("HoldForRelease"));
					claim.setReleasedCount(rs.getString("Released"));
					claim.setAuditCount(rs.getString("HoldForAudit"));
					int total = rs.getInt("PrepForML") + rs.getInt("HoldForReview") + rs.getInt("HoldForRelease")
							+ rs.getInt("Released") + rs.getInt("HoldForAudit");
					claim.setRecievedCount(Integer.toString(total));
					claimlist.add(claim);
				}
			}

		} catch (Exception ex) {

			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();

		}

		return claimlist;
	}

	public static List<ClaimHeader> getClaimDetails(ClaimSummary request) throws SQLException {
		List<ClaimHeader> claimlist = new ArrayList<ClaimHeader>();
		ClaimHeader claim = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionUtil.getConnection();
			String query = "SELECT * FROM hs_claim_Header WHERE health_plan=? and product=? and employer_group=?";
			if (!request.getCurrentClaimStatus().equalsIgnoreCase("Recieved")) {
				query = query + " and claim_status='" + request.getCurrentClaimStatus() + "' ";
			}
			query = query + " limit 200";
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, request.getHealthplan());
			pstmt.setString(2, request.getProduct());
			pstmt.setString(3, request.getEmployergroup());

			System.out.println("QUERY::" + query);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				claim = new ClaimHeader();
				claim.setClaimId(rs.getString("Clm_ID"));
				claim.setAkid(rs.getString("AKID"));
				claim.setClientCd(rs.getString("Client_CD"));
				claim.setClaimFromDate(rs.getString("Clm_From_DT"));
				claim.setClaimToDate(rs.getString("Clm_Through_DT"));
				claim.setClaimCategory(rs.getString("Claim_type"));
				claim.setClaimCurrentStatus(rs.getString("claim_status"));
				claim.setBilledAmt(rs.getString("Billed_Amount"));
				claim.setDrg(rs.getString("DRG"));
				claim.setEmployeeGroup(rs.getString("Employer_Group"));
				claim.setHealthPlan(rs.getString("Health_Plan"));
				claim.setInsuradId(rs.getString("Insured_ID"));
				claim.setNpi(rs.getString("NPI"));
				claim.setAllowedAmt(rs.getDouble("ML_Predicted_Allowed_Amount"));
				claim.setProduct(rs.getString("Product"));
				claim.setDispositionStatus(rs.getString("Disposition_Status"));
				claim.setAuditStatus(rs.getString("audit_status"));
				claim.setPredictionStatus(rs.getString("prediction_status"));
				claim.setLockedBy(rs.getString("locked_by"));
				claim.setDepartmentName(rs.getString("department_name"));
				claim.setFinalAmt(rs.getDouble("Final_Allowed_Amount"));
				claimlist.add(claim);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();
		}
		return claimlist;
	}

	public static List<ClaimHeader> getSelectedClaimDetails(ClaimRequest request) throws SQLException {
		List<ClaimHeader> claimlist = new ArrayList<ClaimHeader>();
		ClaimHeader claim = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int count = 0;
		try {
			conn = ConnectionUtil.getConnection();
			String query = "SELECT * FROM hs_claim_Header where ";

			if (!request.getPredictionMatch().equalsIgnoreCase("All")) {
				if (count > 0)
					query = query + " and Prediction_Status='" + request.getPredictionMatch() + "'" + " and ";
				else
					query = query + "  Prediction_Status='" + request.getPredictionMatch() + "'" + " and ";
			}
			if (!request.getAuditStatus().equalsIgnoreCase("All")) {
				if (count > 0)
					query = query + " and audit_status='" + request.getAuditStatus() + "'" + " and ";
				else
					query = query + "  audit_status='" + request.getAuditStatus() + "'" + " and ";
			}
			if (!request.getClaimStatus().equalsIgnoreCase("All")) {
				if (count > 0)
					query = query + " and claim_Status='" + request.getClaimStatus() + "'" + " and ";
				else
					query = query + " claim_Status='" + request.getClaimStatus() + "'" + " and ";
			}
			if (!request.getAllowedAmount().equalsIgnoreCase("All")) {

				if (request.getAllowedAmount().indexOf('$') > 0) {
					String amt = request.getAllowedAmount().trim().replace("$", "");
					request.setAllowedAmount(amt);
					String amt1 = request.getAllowedAmount().trim().replace(">", "");
					request.setAllowedAmount(amt1);
				}
				if (count > 0)
					query = query + " and ML_Predicted_Allowed_Amount>" + request.getAllowedAmount().trim() + " and ";
				else
					query = query + " ML_Predicted_Allowed_Amount>" + request.getAllowedAmount().trim() + " and ";
			}

			if (!request.getHealthplanType().equalsIgnoreCase("All")
					|| !request.getProductType().equalsIgnoreCase("All")
					|| !request.getEmpgrpType().equalsIgnoreCase("All"))
				query = query + "  ("
						+ JDBClaimUtilMysql.buildQuery(request.getHealthplanType(), request.getProductType(),
								request.getEmpgrpType(), request.getLoginresponse().getModel().getUserId())
						+ " )";
			else
				query = query + "  ("
						+ JDBClaimUtilMysql.buildQuery(request.getHealthplanType(), request.getProductType(),
								request.getEmpgrpType(), request.getLoginresponse().getModel().getUserId())
						+ ")";

			// query=query+JDBClaimUtilMysql.buildQuery(request.getHealthplanType(),request.getProductType(),
			// request.getEmpgrpType(),request.getLoginresponse().getModel().getUserId());
			query=query+" limit 200";
			System.out.println("ClaimDetailQuery::" + query);
			pstmt = conn.prepareStatement(query);

			rs = pstmt.executeQuery();
			while (rs.next()) {
				claim = new ClaimHeader();
				claim.setClaimId(rs.getString("Clm_ID"));
				claim.setAkid(rs.getString("AKID"));
				claim.setClientCd(rs.getString("Client_CD"));
				claim.setClaimFromDate(rs.getString("Clm_From_DT"));
				claim.setClaimToDate(rs.getString("Clm_Through_DT"));
				claim.setClaimCategory(rs.getString("Claim_type"));
				claim.setClaimCurrentStatus(rs.getString("claim_status"));
				claim.setPredictionStatus(rs.getString("prediction_status"));
				claim.setAuditStatus(rs.getString("audit_status"));
				claim.setBilledAmt(rs.getString("Billed_Amount"));
				claim.setDrg(rs.getString("DRG"));
				claim.setEmployeeGroup(rs.getString("Employer_Group"));
				claim.setHealthPlan(rs.getString("Health_Plan"));
				claim.setInsuradId(rs.getString("Insured_ID"));
				claim.setNpi(rs.getString("NPI"));
				claim.setAllowedAmt(rs.getDouble("ML_Predicted_Allowed_Amount"));
				claim.setProduct(rs.getString("Product"));
				claim.setDispositionStatus(rs.getString("Disposition_Status"));
				claim.setLockedBy(rs.getString("locked_by"));
				claim.setDepartmentName(rs.getString("department_name"));

				claim.setFinalAmt(rs.getDouble("Final_Allowed_Amount"));
				claimlist.add(claim);
			}

		} catch (Exception ex) {

			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();

		}

		return claimlist;
	}

	public static ClaimInfoResponse getClaimInfo(ClaimHeader request) throws SQLException {
		ArrayList<ClaimLine> claimlist = new ArrayList<ClaimLine>();
		ClaimInfoResponse response = new ClaimInfoResponse();
		ClaimHeader claimheader = new ClaimHeader();

		ClaimLine claim = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
		try {
			conn = ConnectionUtil.getConnection();
			/*
			 * Modified Here with source_clm_id instead of clm_id and added
			 * ORDER by
			 */
			
			String query = "SELECT * FROM hs_claim_lines WHERE source_clm_id=?  ORDER BY claimline_group_id,record_type ";
			String query1 = "SELECT * FROM hs_claim_Header WHERE clm_Id=?";
			if(request.getClaimCurrentStatus().trim().equalsIgnoreCase("Released"))
			{
				query = "SELECT * FROM hs_claim_lines_released WHERE source_clm_id=?  ORDER BY claimline_group_id,record_type ";
				query1 = "SELECT * FROM hs_claim_Header_released WHERE clm_Id=?";
				
			}
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, request.getClaimId());
			rs = pstmt.executeQuery();
			int linegroupId = 0;
			while (rs.next()) {
				// if((!(rs.getInt("claimline_group_id")==linegroupId)) &&
				// linegroupId > 0){
				linegroupId = rs.getInt("claimline_group_id");
				// claimlist=new ArrayList<ClaimLine>();
				claim = new ClaimLine();
				claim.setClaimId(rs.getString("Clm_ID"));
				claim.setClientCd(rs.getString("Client_CD"));
				claim.setClaimLineNo(rs.getString("Clmline_id"));
				claim.setClaimLineGroupID(rs.getInt("claimline_group_id"));
				claim.setDosFrom(rs.getString("Service_From"));
				claim.setDosTo(rs.getString("Service_TO"));
				claim.setPlaceOfService(rs.getString("Place_Of_Service"));
				claim.setHCPC_CPT(rs.getString("HCPC_CPT"));
				claim.setModifierCode(rs.getString("Modifier_Code"));
				claim.setUnits(rs.getString("Final_Allowed_Units"));
				claim.setBilledCharges(rs.getString("Billed_Charges"));
				claim.setRenderingProviderNPI(rs.getString("Rendering_Provider_NPI"));
				claim.setRevenueCode(rs.getString("Revenue_Code"));
				claim.setLineCurrentStatus(rs.getString("line_current_status"));
				claim.setPredictionStatus(rs.getString("prediction_status"));
				claim.setMlPredictedAllowedAmt(rs.getString("Ml_Predicted_Allowed_Amt"));
				claim.setCreatedBy(rs.getString("Created_By"));
				claim.setFinalAmount(rs.getString("Final_Allowed_Amt"));
				claim.setCreatedOn(rs.getString("Created_On"));
				claim.setRemarks(rs.getString("comments"));
				claim.setRecordType(rs.getString("record_type"));
				claimlist.add(claim);
				/*
				 * }else{ if(linegroupId > 0){
				 * claimlineGroupMap.put(linegroupId,claimlist); } }
				 */
			}
			pstmt1 = conn.prepareStatement(query1);
			pstmt1.setString(1, request.getClaimId());
			rs1 = pstmt1.executeQuery();
			while (rs1.next()) {
				claimheader.setClaimId(rs1.getString("Clm_ID"));
				claimheader.setAkid(rs1.getString("AKID"));
				claimheader.setClientCd(rs1.getString("Client_CD"));
				claimheader.setClaimFromDate(rs1.getString("Clm_From_DT"));
				claimheader.setClaimToDate(rs1.getString("Clm_Through_DT"));
				claimheader.setClaimCategory(rs1.getString("claim_type"));
				claimheader.setClaimCurrentStatus(rs1.getString("claim_status"));
				claimheader.setBilledAmt(rs1.getString("Billed_Amount"));
				claimheader.setDrg(rs1.getString("DRG"));
				claimheader.setEmployeeGroup(rs1.getString("Employer_Group"));
				claimheader.setHealthPlan(rs1.getString("Health_Plan"));
				claimheader.setInsuradId(rs1.getString("Insured_ID"));
				claimheader.setNpi(rs1.getString("NPI"));
				claimheader.setAllowedAmt(rs1.getDouble("ML_Predicted_Allowed_Amount"));
				claimheader.setProduct(rs1.getString("Product"));
				claimheader.setAuditStatus(rs1.getString("audit_status"));
				claimheader.setPredictionStatus(rs1.getString("Prediction_status"));
				claimheader.setLockedBy(rs1.getString("locked_by"));
				claimheader.setDispositionStatus(rs1.getString("Disposition_Status"));
				claimheader.setCreatedOn(rs1.getString("Created_On"));
				claimheader.setLockedBy(rs1.getString("locked_by"));
				claimheader.setFinalAmt(rs1.getDouble("Final_Allowed_Amount"));
				claimheader.setReason(rs1.getString("reason"));
				claimheader.setLockedByUserName(rs1.getString("locked_by_username"));

			}
			response.setClaimlinelist(claimlist);
			response.setClaimheader(claimheader);
		} catch (Exception ex) {
			response.setError(ex.getMessage());
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();
			if (pstmt1 != null)
				pstmt1.close();

		}
		return response;
	}

	
	public static List<ClaimHeader> getClaimList(String searchTerm,String searchQuery,ClaimSearchModel request,Connection connection) throws Exception{

		PreparedStatement statement = null;
		ResultSet resultSet = null;
		List<ClaimHeader> headerList = new ArrayList<ClaimHeader>();
		//searchQuery = "select * from hs_claim_header where Clm_ID in (" + searchTerm.trim() + ")";
		String queue = "";
		
		if (request.getLoginresponse().getClaimsassginedlist() != null) {
	
			for (int i = 0; i < request.getLoginresponse().getClaimsassginedlist().size(); i++) {
				if (i == 0)
					queue = queue + "(";
				else
					queue = queue + " OR (";
				if (request.getLoginresponse().getClaimsassginedlist().get(i).getHealthplan() != null) {
					queue = queue + " health_plan='"
							+ request.getLoginresponse().getClaimsassginedlist().get(i).getHealthplan() + "'";
				}
				if (request.getLoginresponse().getClaimsassginedlist().get(i).getProduct() != null) {
					queue = queue + " and product='"
							+ request.getLoginresponse().getClaimsassginedlist().get(i).getProduct() + "'";
				}
				if (request.getLoginresponse().getClaimsassginedlist().get(i).getEmpgrp() != null) {
					queue = queue + " and client_cd='"
							+ request.getLoginresponse().getClaimsassginedlist().get(i).getClientCd() + "'";
				}
				if (request.getLoginresponse().getClaimsassginedlist().get(i).getEmpgrp() != null) {
					queue = queue + " and employer_group='"
							+ request.getLoginresponse().getClaimsassginedlist().get(i).getEmpgrp() + "')";
				}
	
			}
	
		}
		if (queue.length() > 2) {
			searchQuery = searchQuery + " and (" + queue + ")";
			statement = connection.prepareStatement(searchQuery);
			System.out.println(searchQuery);
			resultSet = statement.executeQuery();
	
			while (resultSet.next()) {
				ClaimHeader claimHeader = new ClaimHeader();
	
				claimHeader.setAkid(resultSet.getString("AKID"));
				claimHeader.setClientCd(resultSet.getString("Client_CD"));
				claimHeader.setClaimId(resultSet.getString("clm_ID"));
				claimHeader.setClaimFromDate(resultSet.getString("Clm_From_DT"));
				claimHeader.setClaimToDate(resultSet.getString("Clm_Through_DT"));
				claimHeader.setClaimCategory(resultSet.getString("Claim_Type"));
				claimHeader.setClaimCurrentStatus(resultSet.getString("claim_status"));
				claimHeader.setHealthPlan(resultSet.getString("Health_Plan"));
				claimHeader.setProduct(resultSet.getString("Product"));
				claimHeader.setEmployeeGroup(resultSet.getString("Employer_Group"));
				claimHeader.setNpi(resultSet.getString("NPI"));
				claimHeader.setPatientName(resultSet.getString("Patient_Name"));
				claimHeader.setPatientBirthDate(resultSet.getString("Patient_BirthDate"));
				claimHeader.setPatientGender(resultSet.getString("Patient_gender"));
				claimHeader.setInsuradId(resultSet.getString("Insured_ID"));
				claimHeader.setInsuredAddress(resultSet.getString("Insured_Address"));
				claimHeader.setInsuredBirthDate(resultSet.getString("Insured_BirthDate"));
				claimHeader.setDrg(resultSet.getString("DRG"));
				claimHeader.setBilledAmt(resultSet.getString("Billed_Amount"));
				claimHeader.setAllowedAmt(resultSet.getDouble("ML_Predicted_Allowed_Amount"));
				claimHeader.setDispositionStatus(resultSet.getString("Disposition_Status"));
				claimHeader.setFiller1(resultSet.getString("Filler1"));
				claimHeader.setFiller2(resultSet.getString("Filler2"));
				claimHeader.setFiller3(resultSet.getString("Filler3"));
				claimHeader.setFiller4(resultSet.getString("Filler4"));
				claimHeader.setFiller5(resultSet.getString("Filler5"));
				claimHeader.setCreatedOn(resultSet.getString("Created_On"));
				claimHeader.setCreatedBy(resultSet.getString("Created_By"));
				claimHeader.setIsNewClaim(resultSet.getString("is_new_claim"));
				claimHeader.setPredictionStatus(resultSet.getString("prediction_status"));
				claimHeader.setAuditStatus(resultSet.getString("audit_status"));
				claimHeader.setLockedBy(resultSet.getString("locked_by"));
				claimHeader.setFinalAmt(resultSet.getDouble("Final_Allowed_Amount"));
	
				claimHeader.setDepartmentName(resultSet.getString("department_name"));
	
				headerList.add(claimHeader);
			}
		}
			return headerList;
		}
	
	/*
	 * this method will get the details of claim based on given claimId input :
	 * Single claimId or multiple claimId with , separated
	 */
	public static List<ClaimHeader> searchClaimIds(String searchTerm, ClaimSearchModel request) throws Exception {
		searchTerm = claimIdWithQuomaSeparated(searchTerm);
		Connection connection = null;
		List<ClaimHeader> headerList = new ArrayList<ClaimHeader>();
		String searchQuery = null;
		try {
			connection = ConnectionUtil.getConnection();
			searchQuery = "select * from hs_claim_header where Clm_ID in (" + searchTerm.trim() + ")";
			headerList = getClaimList(searchTerm,searchQuery,request,connection);
			if (headerList.isEmpty()){
				String searchQueryReleased = "select * from hs_claim_header_released where Clm_ID in (" + searchTerm.trim() + ")";
				List<ClaimHeader> releasedClaimList = getClaimList(searchTerm,searchQueryReleased,request,connection);
				headerList.addAll(releasedClaimList);
			}
			
		}catch(Exception ex){
			System.out.println(" Exception in Search Claimids -- Exception while looking for Claims");
		}
			
		return headerList;
	}

	/*
	 * @Input: "ClaimID,ClaimID"
	 * 
	 * this method will get the input in String format and manipulate the String
	 * to get the Output as below
	 * 
	 * @Output: 'ClaimID','ClaimID'
	 */
	public static String claimIdWithQuomaSeparated(String claimId) {
		StringBuffer claimIdWithQuotes = new StringBuffer();

		if (claimId.contains("\"")) {
			claimId = claimId.replaceAll("\"", "");
		}

		String[] claimIdList = claimId.split(",");

		for (int claimCount = 0; claimCount < claimIdList.length; claimCount++) {
			if (claimCount < claimIdList.length - 1) {
				claimIdWithQuotes.append("'" + claimIdList[claimCount] + "',");
			} else {
				claimIdWithQuotes.append("'" + claimIdList[claimCount] + "'");
			}

		}

		return claimIdWithQuotes.toString();
	}

	/*
	 * @Input: P I Function will get the input as code and get the full name of
	 * 
	 * @Output: Professional Individual
	 */
	public static String categoryNameFromCode(String name) {
		String fullName = null;

		if (name.equalsIgnoreCase("P")) {
			fullName = "Professional";
		}

		if (name.equalsIgnoreCase("I")) {
			fullName = "Individual";
		}

		return fullName;
	}

	public static String editClaimLine(ClaimEditRequest request) throws Exception {
		System.out.println(new Gson().toJson(request));
		Connection connection = null;
		String searchQuery = null;
		PreparedStatement statement = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		String status = null;
		int count = 0;
		try {
			connection = ConnectionUtil.getConnection();
			searchQuery = "insert into hs_claim_header_history(claim_id,claim_info_json,created_date,edited_by)"
					+ "values(?,?,current_timestamp,?)";
			statement = connection.prepareStatement(searchQuery);
			statement.setString(1, request.getClaimHeader().getClaimId());
			statement.setString(2, new Gson().toJson(request.getClaimHeader()));
			// statement.setString(3,request.getClaimHeader().getClaimToDate());
			statement.setInt(3, request.getUser().getUserId());
			statement.executeUpdate();
			String query = "insert into hs_claim_lines_history(claim_id,claim_info_json,created_date,clmline_id,edited_by)"
					+ " values(?,?,current_timestamp,?,?)";
			pstmt = connection.prepareStatement(query);
			pstmt.setString(1, request.getClaimLine().getClaimId());
			pstmt.setString(2, new Gson().toJson(request.getClaimLine()));
			// pstmt.setString(3,request.getClaimLine().getDosTo());
			pstmt.setString(3, request.getClaimLine().getClaimLineNo());
			pstmt.setInt(4, request.getUser().getUserId());

			pstmt.executeUpdate();
			if (request.getNewpredictedAmt() != null && request.getNewpredictedAmt().trim().length() > 0) {
				String query1 = "update hs_claim_lines set Final_Allowed_Amt=?,comments=?,line_current_status=?,remarks=? where Clm_ID=? and Clmline_id=?";
				pstmt1 = connection.prepareStatement(query1);
				pstmt1.setString(1, request.getNewpredictedAmt());
				pstmt1.setString(2, request.getComment());
				pstmt1.setString(3, "Reviewed");
				pstmt1.setString(4, request.getRemarks());
				pstmt1.setString(5, request.getClaimLine().getClaimId());
				pstmt1.setString(6, request.getClaimLine().getClaimLineNo());
				count = pstmt1.executeUpdate();
				if (count > 0)
					status = "Claim with Claim ID=" + request.getClaimLine().getClaimId() + " and Claim Line No="
							+ request.getClaimLine().getClaimLineNo() + " has been Updated";
				else
					status = "Claim with Claim ID=" + request.getClaimLine().getClaimId() + " and Claim Line No="
							+ request.getClaimLine().getClaimLineNo() + " Could not be Updated";
			} else {
				String query1 = "update hs_claim_lines set line_current_status=?,comments=?,remarks=? where Clm_ID=? and Clmline_id=?";
				pstmt1 = connection.prepareStatement(query1);
				pstmt1.setString(1, "Reviewed");
				pstmt1.setString(2, request.getComment());
				pstmt1.setString(3, request.getRemarks());
				pstmt1.setString(4, request.getClaimLine().getClaimId());
				pstmt1.setString(5, request.getClaimLine().getClaimLineNo());
				count = pstmt1.executeUpdate();
				if (count > 0)
					status = "Claim with Claim ID=" + request.getClaimLine().getClaimId() + " and Claim Line No="
							+ request.getClaimLine().getClaimLineNo() + " has been Updated";
				else
					status = "Claim with Claim ID=" + request.getClaimLine().getClaimId() + " and Claim Line No="
							+ request.getClaimLine().getClaimLineNo() + " Could not be Updated";
			}
		} catch (Exception e) {
			status = "Claim with Claim ID=" + request.getClaimLine().getClaimId() + " and Claim Line No="
					+ request.getClaimLine().getClaimLineNo() + " Could not be Updated";
			e.printStackTrace();
		} finally {

			if (connection != null)
				connection.close();
			if (pstmt != null)
				pstmt.close();
			if (pstmt1 != null)
				pstmt1.close();
			if (statement != null)
				statement.close();
		}
		return status;
	}

	public static ClaimInfoResponse lockClaim(ClaimEditRequest request) throws Exception {
		Connection connection = null;
		ClaimInfoResponse response = new ClaimInfoResponse();
		PreparedStatement pstmt1 = null;
		int count = 0;
		try {
			connection = ConnectionUtil.getConnection();
			String query1 = "update hs_claim_header set locked_by=" + request.getUser().getUserId()
					+ ",department_name='" + request.getUser().getDepartmentName() + "',locked_by_username='"
					+ request.getUser().getUserName() + "' where clm_id=?";
			System.out.println("query" + query1);
			pstmt1 = connection.prepareStatement(query1);
			pstmt1.setString(1, request.getClaimHeader().getClaimId());
			count = pstmt1.executeUpdate();

			if (count < 0)
				response.setError(
						"Claim with Claim ID=" + request.getClaimHeader().getClaimId() + " Could not be Locked");
		} catch (Exception e) {
			response.setError("Claim with Claim ID=" + request.getClaimHeader().getClaimId() + " Could not be Locked");
			e.printStackTrace();
		} finally {

			if (connection != null)
				connection.close();
			if (pstmt1 != null)
				pstmt1.close();

		}
		return response;
	}

	public static ClaimInfoResponse submitDecision(ClaimDecision request) throws Exception {

		ClaimInfoResponse response = new ClaimInfoResponse();
		Connection connection = null;
		String searchQuery = null;
		PreparedStatement statement = null;
		PreparedStatement pstmt = null;

		String query = "update hs_claim_header set claim_status=?";
		String claimStat = null;

		int count = 0;
		try {
			if (request.getReason() != null) {
				request.getClaiminforeponse().getClaimheader().setReason(request.getReason());
			}
			connection = ConnectionUtil.getConnection();
			searchQuery = "insert into hs_claim_header_history(claim_id,claim_info_json,date_of_lastupdate,created_date,edited_by)"
					+ "values(?,?,?,current_timestamp,?)";
			statement = connection.prepareStatement(searchQuery);
			statement.setString(1, request.getClaiminforeponse().getClaimheader().getClaimId());
			statement.setString(2, new Gson().toJson(request.getClaiminforeponse().getClaimheader()));
			statement.setString(3, request.getClaiminforeponse().getClaimheader().getClaimToDate());
			statement.setInt(4, request.getUser().getUserId());
			statement.executeUpdate();

			if (request.getClaimDecisionType().equalsIgnoreCase("Audit")) {
				if (request.getDecision().equalsIgnoreCase("accept")) {
					claimStat = "Hold For Release";
					query = query + ",audit_status='Audit Done'";

				} else {
					claimStat = "Hold For Review";

					query = query + ",locked_by=" + null + ",audit_status='No Audit'";
				}

			} else {
				if (request.getDecision().equalsIgnoreCase("accept")) {
					claimStat = "Released";

					query = query + ",audit_status='Audit Done'";

				} else {
					claimStat = "Hold For Review";
					query = query + ",locked_by=" + null + ",audit_status='No Audit'";
				}
			}
			query = query + " ,remarks=?,locked_by=null,reason=?";
			query = query + " where clm_id=" + request.getClaiminforeponse().getClaimheader().getClaimId();
			pstmt = connection.prepareStatement(query);
			pstmt.setString(2, request.getRemarks());
			pstmt.setString(1, claimStat);
			pstmt.setString(3, request.getReason());

			count = pstmt.executeUpdate();
			System.out.println(count);
			if (count > 0) {
				response.setInfo("Decsion Submitted Successfully");
			} else {
				response.setError("Decsion Submission Un-Successful");
			}

			if (!request.getClaimDecisionType().equalsIgnoreCase("Audit") && request.getDecision().equalsIgnoreCase("accept")) {
				//
				String releaseHeader = "insert into hs_claim_header_released (AKID,Run_ID,batch_id,Client_CD,Clm_ID,Clm_From_DT,Clm_Through_DT,Claim_Type,claim_state,Claim_Status,Audit_Status,prediction_status,Health_Plan,Product,Employer_Group,NPI,provider_name,Patient_Name,Patient_BirthDate,Patient_gender,Insured_ID,Insured_Address,Insured_BirthDate,DRG,Billed_Amount,Final_Allowed_Amount,ML_Predicted_Allowed_Amount,Disposition_Status,is_auto_examined,is_auto_audited,is_new_claim,Filler1,Filler2,Filler3,Filler4,Filler5,locked_by,remarks,department_name,reason,locked_by_username,Claim_JSON,Created_On,Created_By)"
						+ " select AKID,Run_ID,batch_id,Client_CD,Clm_ID,Clm_From_DT,Clm_Through_DT,Claim_Type,claim_state,Claim_Status,Audit_Status,prediction_status,Health_Plan,Product,Employer_Group,NPI,provider_name,Patient_Name,Patient_BirthDate,Patient_gender,Insured_ID,Insured_Address,Insured_BirthDate,DRG,Billed_Amount,Final_Allowed_Amount,ML_Predicted_Allowed_Amount,Disposition_Status,is_auto_examined,is_auto_audited,is_new_claim,Filler1,Filler2,Filler3,Filler4,Filler5,locked_by,remarks,department_name,reason,locked_by_username,Claim_JSON,Created_On,Created_By"
						+ " from hs_claim_header where clm_id="
						+ request.getClaiminforeponse().getClaimheader().getClaimId();
				System.out.println(releaseHeader);
				PreparedStatement psHeader = connection.prepareStatement(releaseHeader);
				psHeader.executeUpdate();

				String releaseLine = "insert into hs_claim_lines_released (akid,run_id,batch_id,clm_ID,clmline_ID,claimline_group_id,record_type,source_clm_id,source_clmline_id,line_current_status,Client_CD,Service_From,Service_TO,Claim_Type,Claim_Status,Prediction_Status,Audit_Status,Health_Plan,Product,Employer_Group,productrategroup,Place_Of_Service,HCPC_CPT,Modifier_Code,Modifier_Code2,Billed_Units,Billed_Charges,Rendering_Provider_NPI,Attending_Physician_NPI,Revenue_Code,Final_Allowed_Units,Final_Allowed_Amt,ml_predicted_allowed_amt,prediction_accuracy,prediction_variation,prediction_variation_pct,Disposition1,Disposition2,Disposition3,Filler1,Filler2,Filler3,Filler4,Filler5,Filler6,comments,remarks,claim_Json,Created_On,Created_By)"
						+ " select akid,run_id,batch_id,clm_ID,clmline_ID,claimline_group_id,record_type,source_clm_id,source_clmline_id,line_current_status,Client_CD,Service_From,Service_TO,Claim_Type,Claim_Status,Prediction_Status,Audit_Status,Health_Plan,Product,Employer_Group,productrategroup,Place_Of_Service,HCPC_CPT,Modifier_Code,Modifier_Code2,Billed_Units,Billed_Charges,Rendering_Provider_NPI,Attending_Physician_NPI,Revenue_Code,Final_Allowed_Units,Final_Allowed_Amt,ml_predicted_allowed_amt,prediction_accuracy,prediction_variation,prediction_variation_pct,Disposition1,Disposition2,Disposition3,Filler1,Filler2,Filler3,Filler4,Filler5,Filler6,comments,remarks,claim_Json,Created_On,Created_By"
						+ " from hs_claim_lines where source_clm_id="					
						+ request.getClaiminforeponse().getClaimheader().getClaimId();
				System.out.println(releaseLine);
				PreparedStatement psLines = connection.prepareStatement(releaseLine);
				psLines.executeUpdate();


			}
		} catch (Exception e) {
			response.setError("Decision Submission Un-Successful");
			e.printStackTrace();
		} finally {

			if (connection != null)
				connection.close();
			if (pstmt != null)
				pstmt.close();

			if (statement != null)
				statement.close();
		}
		return response;
	}


	
  private static List<ClaimSummary> getInProcessClaimList(ClaimStatus request,String workQueue) throws Exception{
		System.out.println(new Gson().toJson(request));
		List<ClaimSummary> claimlist = new ArrayList<ClaimSummary>();
		ClaimSummary claim = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int count = 0;
		
		try {
			conn = ConnectionUtil.getConnection();
			String query = "SELECT a.health_plan,a.employer_group,a.product,SUM(a.MLPrepCount) AS PrepForML,"
					+ " SUM(a.ReviewCount) AS HoldForReview, SUM(a.AuditCount) AS HoldForAudit"
					+ " ,SUM(a.ForReleaseCount) AS HoldForRelease,SUM(a.ReleasedCount) AS Released"
					+ " FROM (SELECT health_plan,employer_group,product,CASE claim_status WHEN 'Prep For ML' THEN COUNT(*)"
					+ " ELSE 0 END AS MLPrepCount,CASE claim_status WHEN 'Hold For Review' THEN COUNT(*) ELSE 0 END AS ReviewCount,"
					+ " CASE claim_status WHEN 'Hold For Audit' THEN COUNT(*) ELSE 0 END AS AuditCount,"
					+ " CASE claim_status WHEN 'Hold For Release' THEN COUNT(*) ELSE 0 END AS ForReleaseCount,"
					+ " CASE claim_status WHEN 'Released' THEN COUNT(*) ELSE 0 END AS ReleasedCount FROM hs_claim_header where ";
		query = query + workQueue + ")";

		if (request.getFromDate() != null && request.getFromDate().trim().length() > 0) {
			query = query + " and created_on>= '" + request.getFromDate() + " 00:00:01'";
			count++;
		}
		if (request.getToDate() != null && request.getToDate().trim().length() > 0) {

			query = query + " and created_on<= '" + request.getToDate() + " 23:59:59 '";

		}
		query = query
				+ " GROUP BY health_plan,employer_group,product,claim_status) a GROUP BY a.health_plan,a.employer_group,a.product";
		System.out.println("Query::" + query);
		pstmt = conn.prepareStatement(query);
		rs = pstmt.executeQuery();

		while (rs.next()) {
			claim = new ClaimSummary();
			claim.setHealthplan(rs.getString("health_plan"));
			claim.setProduct(rs.getString("product"));
			claim.setEmployergroup(rs.getString("employer_group"));
			claim.setPrepcount(rs.getString("PrepForML"));
			claim.setReviewCount(rs.getString("HoldForReview"));
			claim.setReleaseCount(rs.getString("HoldForRelease"));
			claim.setReleasedCount(rs.getString("Released"));
			claim.setAuditCount(rs.getString("HoldForAudit"));
			int total = rs.getInt("PrepForML") + rs.getInt("HoldForReview") + rs.getInt("HoldForRelease")
					+ rs.getInt("Released") + rs.getInt("HoldForAudit");
			claim.setRecievedCount(Integer.toString(total));
			claimlist.add(claim);
		}

	} catch (Exception ex) {
		ex.printStackTrace();
	} finally {
		if (conn != null)
			conn.close();
		if (pstmt != null)
			pstmt.close();
	}
		
	  return claimlist;
	  
  }

  private static List<ClaimSummary> getReleasedClaimList(ClaimStatus request,String workQueue) throws Exception{
		System.out.println(new Gson().toJson(request));
		List<ClaimSummary> claimlist = new ArrayList<ClaimSummary>();
		ClaimSummary claim = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int count = 0;
		
		try {
			conn = ConnectionUtil.getConnection();
			String query = "SELECT a.health_plan,a.employer_group,a.product,SUM(a.ReleasedCount) AS Released"
					+ " FROM (SELECT health_plan,employer_group,product,COUNT(*) AS ReleasedCount FROM hs_claim_header_released where ";
		query = query + workQueue + ")";

		if (request.getFromDate() != null && request.getFromDate().trim().length() > 0) {
			query = query + " and created_on>= '" + request.getFromDate() + " 00:00:01'";
			count++;
		}
		if (request.getToDate() != null && request.getToDate().trim().length() > 0) {

			query = query + " and created_on<= '" + request.getToDate() + " 23:59:59 '";

		}
		query = query
				+ " GROUP BY health_plan,employer_group,product,claim_status) a GROUP BY a.health_plan,a.employer_group,a.product";
		System.out.println("Query::" + query);
		pstmt = conn.prepareStatement(query);
		rs = pstmt.executeQuery();

		while (rs.next()) {
			claim = new ClaimSummary();
			claim.setHealthplan(rs.getString("health_plan"));
			claim.setProduct(rs.getString("product"));
			claim.setEmployergroup(rs.getString("employer_group"));
			claim.setReleasedCount(rs.getString("Released"));
			claimlist.add(claim);
		}

	} catch (Exception ex) {
		ex.printStackTrace();
	} finally {
		if (conn != null)
			conn.close();
		if (pstmt != null)
			pstmt.close();
	}
		
	  return claimlist;
	  
  }

	
	public static List<ClaimSummary> getClaimStatusList(ClaimStatus request) throws Exception {
			List<ClaimSummary> claimlist = new ArrayList<ClaimSummary>();
			List<ClaimSummary> releasedClaimList = new ArrayList<ClaimSummary>();
			List<ClaimSummary> inprocessClaimList = new ArrayList<ClaimSummary>();
			String queue = null;
			try {
				if (request.getResponse().getClaimsassginedlist() != null) {
					queue = "(";
					for (int i = 0; i < request.getResponse().getClaimsassginedlist().size(); i++) {
						if (i == 0)
							queue = queue + "(";
						else
							queue = queue + " OR (";
						if (request.getResponse().getClaimsassginedlist().get(i).getHealthplan() != null) {
							queue = queue + " health_plan='"
									+ request.getResponse().getClaimsassginedlist().get(i).getHealthplan() + "'";
						}
						if (request.getResponse().getClaimsassginedlist().get(i).getProduct() != null) {
							queue = queue + " and product='"
									+ request.getResponse().getClaimsassginedlist().get(i).getProduct() + "'";
						}
						if (request.getResponse().getClaimsassginedlist().get(i).getEmpgrp() != null) {
							queue = queue + " and client_cd='"
									+ request.getResponse().getClaimsassginedlist().get(i).getClientCd() + "'";
						}
						if (request.getResponse().getClaimsassginedlist().get(i).getEmpgrp() != null) {
							queue = queue + " and employer_group='"
									+ request.getResponse().getClaimsassginedlist().get(i).getEmpgrp() + "')";
						}
	
					}
	
				}
	
				inprocessClaimList = getInProcessClaimList(request,queue);
				releasedClaimList = getReleasedClaimList(request,queue);
				claimlist =  mergeClaimSummary(inprocessClaimList, releasedClaimList);
				
		}catch(Exception ex){
			System.out.println(ex.getMessage());
		}
		return claimlist;
	}
	
	
	
	
	
	

	public static void updateAuditStatus(ClaimInfoResponse request, String status) throws SQLException {
		Connection connection = null;
		String searchQuery = null;
		PreparedStatement statement = null;
		PreparedStatement pstmt1 = null;

		try {
			connection = ConnectionUtil.getConnection();
			searchQuery = "insert into hs_claim_header_history(claim_id,claim_info_json,date_of_lastupdate,created_date)"
					+ "values(?,?,?,current_timestamp)";
			statement = connection.prepareStatement(searchQuery);
			statement.setString(1, request.getClaimheader().getClaimId());
			statement.setString(2, new Gson().toJson(request.getClaimheader()));
			statement.setString(3, request.getClaimheader().getClaimToDate());

			statement.executeUpdate();

			String query1 = "update hs_claim_header set claim_status=?,locked_by=null where Clm_ID=?";
			pstmt1 = connection.prepareStatement(query1);
			if (status.equalsIgnoreCase("No"))
				pstmt1.setString(1, "Hold For Release");
			else
				pstmt1.setString(1, "Hold For Audit");
			pstmt1.setString(2, request.getClaimheader().getClaimId());

			pstmt1.executeUpdate();

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (connection != null)
				connection.close();
			if (statement != null)
				statement.close();
			if (pstmt1 != null)
				pstmt1.close();
		}

	}

	public static String acceptClaimLine(ClaimEditRequest request) throws SQLException {
		System.out.println(new Gson().toJson(request));
		Connection connection = null;
		String searchQuery = null;
		PreparedStatement statement = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		String status = null;
		int count = 0;
		try {
			connection = ConnectionUtil.getConnection();
			searchQuery = "insert into hs_claim_header_history(claim_id,claim_info_json,date_of_lastupdate,created_date,edited_by)"
					+ "values(?,?,?,current_timestamp,?)";
			statement = connection.prepareStatement(searchQuery);
			statement.setString(1, request.getClaimHeader().getClaimId());
			statement.setString(2, new Gson().toJson(request.getClaimHeader()));
			statement.setString(3, request.getClaimHeader().getClaimToDate());
			statement.setInt(4, request.getUser().getUserId());
			statement.executeUpdate();
			String query = "insert into hs_claim_lines_history(claim_id,claim_info_json,date_of_lastupdate,created_date,clmline_id,edited_by)"
					+ " values(?,?,?,current_timestamp,?,?)";
			pstmt = connection.prepareStatement(query);
			pstmt.setString(1, request.getClaimLine().getClaimId());
			pstmt.setString(2, new Gson().toJson(request.getClaimLine()));
			pstmt.setString(3, request.getClaimLine().getDosTo());
			pstmt.setString(4, request.getClaimLine().getClaimLineNo());
			pstmt.setInt(5, request.getUser().getUserId());

			pstmt.executeUpdate();

			String query1 = "update hs_claim_lines set line_current_status=?,Final_Allowed_Amt=? where Clm_ID=? and Clmline_id=?";
			pstmt1 = connection.prepareStatement(query1);
			pstmt1.setString(1, "Reviewed");
			pstmt1.setString(2, request.getClaimLine().getMlPredictedAllowedAmt());
			pstmt1.setString(3, request.getClaimLine().getClaimId());
			pstmt1.setString(4, request.getClaimLine().getClaimLineNo());
			count = pstmt1.executeUpdate();
			if (count > 0)
				status = "Claim with Claim ID=" + request.getClaimLine().getClaimId() + " and Claim Line No="
						+ request.getClaimLine().getClaimLineNo() + " has been Updated";
			else
				status = "Claim with Claim ID=" + request.getClaimLine().getClaimId() + " and Claim Line No="
						+ request.getClaimLine().getClaimLineNo() + " Could not be Updated";

		} catch (Exception e) {
			status = "Claim with Claim ID=" + request.getClaimLine().getClaimId() + " and Claim Line No="
					+ request.getClaimLine().getClaimLineNo() + " Could not be Updated";
			e.printStackTrace();
		} finally {

			if (connection != null)
				connection.close();
			if (pstmt != null)
				pstmt.close();
			if (pstmt1 != null)
				pstmt1.close();
			if (statement != null)
				statement.close();
		}
		return status;
	}

	public static List<ClaimHeader> lockedClaimsList(ClaimLockRequest request) throws SQLException {
		System.out.println(new Gson().toJson(request));
		List<ClaimHeader> claimlist = new ArrayList<ClaimHeader>();
		ClaimHeader claim = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionUtil.getConnection();
			if (request.getType().equalsIgnoreCase("Me")) {
				String query = "SELECT * FROM hs_claim_Header WHERE locked_by=? ";
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, request.getUser().getUserId());
				System.out.println("Query::" + query);
			} else {
				String query = "SELECT * FROM hs_claim_Header WHERE locked_by is not null and department_name=? and locked_by!=?";
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, request.getUser().getDepartmentName());
				pstmt.setInt(2, request.getUser().getUserId());
				System.out.println("Query::" + query);

			}

			rs = pstmt.executeQuery();
			while (rs.next()) {
				claim = new ClaimHeader();
				claim.setClaimId(rs.getString("Clm_ID"));
				claim.setAkid(rs.getString("AKID"));
				claim.setClientCd(rs.getString("Client_CD"));
				claim.setClaimFromDate(rs.getString("Clm_From_DT"));
				claim.setClaimToDate(rs.getString("Clm_Through_DT"));
				claim.setClaimCategory(rs.getString("Claim_Type"));
				claim.setClaimCurrentStatus(rs.getString("claim_status"));
				claim.setBilledAmt(rs.getString("Billed_Amount"));
				claim.setDrg(rs.getString("DRG"));
				claim.setEmployeeGroup(rs.getString("Employer_Group"));
				claim.setHealthPlan(rs.getString("Health_Plan"));
				claim.setInsuradId(rs.getString("Insured_ID"));
				claim.setNpi(rs.getString("NPI"));
				claim.setAllowedAmt(rs.getDouble("ML_Predicted_Allowed_Amount"));
				claim.setProduct(rs.getString("Product"));
				claim.setDispositionStatus(rs.getString("Disposition_Status"));
				claim.setAuditStatus(rs.getString("audit_status"));
				claim.setPredictionStatus(rs.getString("prediction_status"));
				claim.setLockedBy(rs.getString("locked_by"));
				claim.setDepartmentName(rs.getString("department_name"));
				claim.setFinalAmt(rs.getDouble("Final_allowed_amount"));
				claimlist.add(claim);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();
		}
		return claimlist;
	}

	public static int lockedClaimsCount(UserModel request) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int count = 0;
		try {
			conn = ConnectionUtil.getConnection();
			String query = "SELECT count(*) FROM hs_claim_Header WHERE locked_by=? ";
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, request.getUserId());
			rs = pstmt.executeQuery();
			while (rs.next()) {
				count = rs.getInt("count(*)");
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();
		}
		return count;

	}

	public static boolean unlockClaim(ClaimLockRequest request) throws SQLException {

		Connection conn = null;
		PreparedStatement pstmt = null;
		int count = 0;
		boolean value = false;
		try {
			conn = ConnectionUtil.getConnection();
			String query = "update  hs_claim_Header set locked_by=null,department_name=null where clm_id=?";
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, request.getClaimheader().getClaimId());
			count = pstmt.executeUpdate();
			if (count > 0)
				value = true;

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();
		}

		return value;
	}

	public static List<MLResults> predictionResults(PredictionResultsRequest request) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		MLResults results = null;
		List<MLResults> list = new ArrayList<MLResults>();
		try {
			conn = ConnectionUtil.getConnection();
			String query = "select * from ml_results";
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				results = new MLResults();
				results.setAllowedAmt(rs.getString("AllowedAmt"));
				results.setDistance(rs.getString("distance"));
				results.setIndex(rs.getString("index"));
				results.setMatchclmid(rs.getString("match_clm_id"));
				results.setMatchclmline(rs.getString("match_clm_line"));
				results.setNeighbor(rs.getString("neighbor"));
				results.setPredictions(rs.getString("predictions"));
				results.setSourceclmid(rs.getString("source_clm_id"));
				results.setSourceclmline(rs.getString("source_clm_line"));
				list.add(results);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();
		}

		return list;
	}

	public static List<MLMatchClaimLine> mlTestData(PredictionResultsRequest request) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		MLMatchClaimLine results = null;
		String queue = null;
		List<MLMatchClaimLine> list = new ArrayList<MLMatchClaimLine>();
		try {
			String query = "";
			conn = ConnectionUtil.getConnection();
			if (request.getType().equalsIgnoreCase("testdata"))
				query = "select * from ml_test where ";
			else
				query = "select * from ml_train where ";

			if (request.getLoginresponse().getClaimsassginedlist() != null) {
				queue = "(";

				for (int i = 0; i < request.getLoginresponse().getClaimsassginedlist().size(); i++) {
					if (i == 0)
						queue = queue + "(";
					else
						queue = queue + " OR (";
					if (request.getLoginresponse().getClaimsassginedlist().get(i).getHealthplan() != null) {
						queue = queue + " health_plan='"
								+ request.getLoginresponse().getClaimsassginedlist().get(i).getHealthplan() + "'";
					}
					if (request.getLoginresponse().getClaimsassginedlist().get(i).getProduct() != null) {
						queue = queue + " and product='"
								+ request.getLoginresponse().getClaimsassginedlist().get(i).getProduct() + "'";
					}

					if (request.getLoginresponse().getClaimsassginedlist().get(i).getEmpgrp() != null) {
						queue = queue + " and employer_group='"
								+ request.getLoginresponse().getClaimsassginedlist().get(i).getEmpgrp() + "')";
					}

				}
				queue = queue + ")";

			}
			query = query + queue;
			System.out.println("query:::" + query);
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				results = new MLMatchClaimLine();
				results.setId(rs.getString("id"));
				results.setProduct(rs.getString("Product"));
				list.add(results);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();
		}

		return list;
	}

	public static PredictionResultsResponse predictionReviewSummary(PredictionResultsRequest request)
			throws SQLException {
		PredictionResultsResponse response = new PredictionResultsResponse();
		Connection conn = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;
		MLMatchClaimLine incomingData = new MLMatchClaimLine();
		MLMatchClaimLine trainingData = new MLMatchClaimLine();
		MLResults prediction = new MLResults();
		try {
			conn = ConnectionUtil.getConnection();
			String query2 = "select * from ml_results where source_clm_id=?";
			pstmt2 = conn.prepareStatement(query2);
			pstmt2.setString(1, request.getClaimID());
			rs2 = pstmt2.executeQuery();
			while (rs2.next()) {
				prediction = new MLResults();
				prediction.setAllowedAmt(rs2.getString("AllowedAmt"));
				prediction.setDistance(rs2.getString("distance"));
				prediction.setIndex(rs2.getString("index"));
				prediction.setMatchclmid(rs2.getString("match_clm_id"));
				prediction.setMatchclmline(rs2.getString("match_clm_line"));
				prediction.setNeighbor(rs2.getString("neighbor"));
				prediction.setPredictions(rs2.getString("predictions"));
				prediction.setSourceclmid(rs2.getString("source_clm_id"));
				prediction.setSourceclmline(rs2.getString("source_clm_line"));

			}

			String query = "select * from ml_test where clm_id=?";
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, request.getClaimID());
			rs = pstmt.executeQuery();
			while (rs.next()) {
				incomingData = new MLMatchClaimLine();
				incomingData.setId(rs.getString("id"));
			}
			if (prediction.getMatchclmid() != null) {
				String query1 = "select * from ml_train where clm_id=?";
				pstmt1 = conn.prepareStatement(query1);
				pstmt1.setString(1, prediction.getMatchclmid());
				rs1 = pstmt1.executeQuery();
				while (rs1.next()) {
					trainingData = new MLMatchClaimLine();
					trainingData.setId(rs1.getString("id"));

				}

			}

			response.setInfo("Processed Successfully");
			response.setIncomingData(incomingData);
			response.setPrediction(prediction);
			response.setTrainingData(trainingData);
		} catch (Exception ex) {
			response.setError("Error Has Occured While Processing the request");
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();
			if (pstmt1 != null)
				pstmt1.close();
			if (pstmt2 != null)
				pstmt2.close();
		}

		return response;
	}

	public static boolean releaseAllClaims(ClaimSummary request) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		PreparedStatement pstmt3 = null;
		PreparedStatement pstmt4 = null;

		int count = 0;
		boolean value = false;
		try {
			conn = ConnectionUtil.getConnection();
			String query = "update hs_claim_header set Claim_Status='Released' where Health_Plan=? and"
					+ " Product=? and Employer_Group=? and claim_status='Hold For Release'";
			
			String releaseHeader = "insert into hs_claim_header_released (AKID,Run_ID,batch_id,Client_CD,Clm_ID,Clm_From_DT,Clm_Through_DT,Claim_Type,claim_state,Claim_Status,Audit_Status,prediction_status,Health_Plan,Product,Employer_Group,NPI,provider_name,Patient_Name,Patient_BirthDate,Patient_gender,Insured_ID,Insured_Address,Insured_BirthDate,DRG,Billed_Amount,Final_Allowed_Amount,ML_Predicted_Allowed_Amount,Disposition_Status,is_auto_examined,is_auto_audited,is_new_claim,Filler1,Filler2,Filler3,Filler4,Filler5,locked_by,remarks,department_name,reason,locked_by_username,Claim_JSON,Created_On,Created_By)"
					+ " select AKID,Run_ID,batch_id,Client_CD,Clm_ID,Clm_From_DT,Clm_Through_DT,Claim_Type,claim_state,Claim_Status,Audit_Status,prediction_status,Health_Plan,Product,Employer_Group,NPI,provider_name,Patient_Name,Patient_BirthDate,Patient_gender,Insured_ID,Insured_Address,Insured_BirthDate,DRG,Billed_Amount,Final_Allowed_Amount,ML_Predicted_Allowed_Amount,Disposition_Status,is_auto_examined,is_auto_audited,is_new_claim,Filler1,Filler2,Filler3,Filler4,Filler5,locked_by,remarks,department_name,reason,locked_by_username,Claim_JSON,Created_On,Created_By"
					+ " from hs_claim_header where Health_Plan=? and"
					+ " Product=? and Employer_Group=? and claim_status='Released'";
			
			

			String releaseLine = "insert into hs_claim_lines_released (akid,run_id,batch_id,clm_ID,clmline_ID,claimline_group_id,record_type,source_clm_id,source_clmline_id,line_current_status,Client_CD,Service_From,Service_TO,Claim_Type,Claim_Status,Prediction_Status,Audit_Status,Health_Plan,Product,Employer_Group,productrategroup,Place_Of_Service,HCPC_CPT,Modifier_Code,Modifier_Code2,Billed_Units,Billed_Charges,Rendering_Provider_NPI,Attending_Physician_NPI,Revenue_Code,Final_Allowed_Units,Final_Allowed_Amt,ml_predicted_allowed_amt,prediction_accuracy,prediction_variation,prediction_variation_pct,Disposition1,Disposition2,Disposition3,Filler1,Filler2,Filler3,Filler4,Filler5,Filler6,comments,remarks,claim_Json,Created_On,Created_By)"
					+ " select akid,run_id,batch_id,clm_ID,clmline_ID,claimline_group_id,record_type,source_clm_id,source_clmline_id,line_current_status,Client_CD,Service_From,Service_TO,Claim_Type,Claim_Status,Prediction_Status,Audit_Status,Health_Plan,Product,Employer_Group,productrategroup,Place_Of_Service,HCPC_CPT,Modifier_Code,Modifier_Code2,Billed_Units,Billed_Charges,Rendering_Provider_NPI,Attending_Physician_NPI,Revenue_Code,Final_Allowed_Units,Final_Allowed_Amt,ml_predicted_allowed_amt,prediction_accuracy,prediction_variation,prediction_variation_pct,Disposition1,Disposition2,Disposition3,Filler1,Filler2,Filler3,Filler4,Filler5,Filler6,comments,remarks,claim_Json,Created_On,Created_By"
					+ " from hs_claim_lines where Health_Plan=? and"
					+ " Product=? and Employer_Group=? and claim_status='Released'";
			
			String deleteHeader = "delete from hs_claim_header where Health_Plan=? and"
					+ " Product=? and Employer_Group=? and claim_status='Released'";
		

			String deleteLine = "delete from hs_claim_lines where  Health_Plan=? and"
					+ " Product=? and Employer_Group=? and claim_status='Released'";
			
			
			
			
			
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, request.getHealthplan());
			pstmt.setString(2, request.getProduct());
			pstmt.setString(3, request.getEmployergroup());
			count = pstmt.executeUpdate();
			
			pstmt1 = conn.prepareStatement(releaseHeader);
			pstmt1.setString(1, request.getHealthplan());
			pstmt1.setString(2, request.getProduct());
			pstmt1.setString(3, request.getEmployergroup());
			pstmt1.executeUpdate();
			
			
			pstmt2 = conn.prepareStatement(releaseLine);
			pstmt2.setString(1, request.getHealthplan());
			pstmt2.setString(2, request.getProduct());
			pstmt2.setString(3, request.getEmployergroup());
			pstmt2.executeUpdate();
			
			
			pstmt3 = conn.prepareStatement(deleteHeader);
			pstmt3.setString(1, request.getHealthplan());
			pstmt3.setString(2, request.getProduct());
			pstmt3.setString(3, request.getEmployergroup());
			pstmt3.executeUpdate();
			
			
			pstmt4 = conn.prepareStatement(deleteLine);
			pstmt4.setString(1, request.getHealthplan());
			pstmt4.setString(2, request.getProduct());
			pstmt4.setString(3, request.getEmployergroup());
			pstmt4.executeUpdate();
			
			
			if (count > 0)
				value = true;

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();
		}
		return value;
	}

	public static List<MLMatchClaimLine> getSeletcedPredictionList(ClaimRequest request) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		MLMatchClaimLine results = null;
		List<MLMatchClaimLine> list = new ArrayList<MLMatchClaimLine>();

		try {
			String query = "select * from ml_train where ";
			conn = ConnectionUtil.getConnection();

			query = query + JDBClaimUtilMysql.buildTraningQuery(request.getHealthplanType(), request.getProductType(),
					request.getEmpgrpType(), request.getLoginresponse().getModel().getUserId());

			// if(!request.getProductType().equalsIgnoreCase("All")||
			// !request.getHealthplanType().equalsIgnoreCase("All"))
			// {
			// query=query+" where ";
			// }
			// if(!request.getHealthplanType().equalsIgnoreCase("All"))
			// {
			// query=query+" healthplan='"+request.getHealthplanType()+"'";
			// count++;
			// }
			// if(!request.getProductType().equalsIgnoreCase("All"))
			// {
			// if(count>0)
			// {
			// query=query+"and ";
			// }
			// query=query+" product='"+request.getProductType()+"'";
			// count++;
			// }
			pstmt = conn.prepareStatement(query);
			System.out.println("query" + query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				results = new MLMatchClaimLine();

				results.setId(rs.getString("id"));

				results.setProduct(rs.getString("Product"));
				list.add(results);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();
		}

		return list;
	}

	public static List<String> reasonsList() throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<String> reasonslist = new ArrayList<String>();
		try {
			conn = ConnectionUtil.getConnection();
			String query = "select * from hs_claim_reason";
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				reasonslist.add(rs.getString("reasons"));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();
		}

		return reasonslist;
	}

	public static List<ClaimHeader> getmyAssignedClaims(UserModel request) throws SQLException {
		List<ClaimHeader> list = new ArrayList<ClaimHeader>();
		List<UserPermissions> permissionlist = new ArrayList<UserPermissions>();
		UserPermissions permission = new UserPermissions();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
		ClaimHeader header = new ClaimHeader();

		try {
			conn = ConnectionUtil.getConnection();
			String query = "select * from hs_user_workqueue where userid=? and active='Y'";
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, request.getUserId());
			rs = pstmt.executeQuery();
			while (rs.next()) {
				permission = new UserPermissions();
				permission.setEmpgrp(rs.getString("Employer_Group"));
				permission.setHealthplan(rs.getString("Health_Plan"));
				permission.setProduct(rs.getString("Product"));
				permission.setStatus(rs.getString("status"));
				permission.setClientCd(rs.getString("client_cd"));
				permissionlist.add(permission);

			}
			String query1 = "select * from hs_claim_header where (locked_by is null or locked_by=?) and ";
			if (permissionlist != null) {
				query1 = query1 + "(";
				for (int i = 0; i < permissionlist.size(); i++) {

					if (i == 0)
						query1 = query1 + "(";
					else
						query1 = query1 + " OR (";
					if (permissionlist.get(i).getHealthplan() != null) {
						query1 = query1 + " health_plan='" + permissionlist.get(i).getHealthplan() + "'";
					}
					if (permissionlist.get(i).getProduct() != null) {
						query1 = query1 + " and product='" + permissionlist.get(i).getProduct() + "'";
					}
					if (permissionlist.get(i).getStatus() != null) {
						query1 = query1 + " and Claim_Status='" + permissionlist.get(i).getStatus() + "'";
					}
					if (permissionlist.get(i).getEmpgrp() != null) {
						query1 = query1 + " and employer_group='" + permissionlist.get(i).getEmpgrp() + "')";
					}

				}
				query1 = query1 + ") order by created_on asc limit 100";
			}

			
			System.out.println("**** MY WORK QUEUE Query::" + query1);
			pstmt1 = conn.prepareStatement(query1);
			pstmt1.setInt(1, request.getUserId());
			rs1 = pstmt1.executeQuery();
			while (rs1.next()) {
				header = new ClaimHeader();
				header.setClaimId(rs1.getString("clm_id"));
				header.setAkid(rs1.getString("AKID"));
				header.setClientCd(rs1.getString("Client_CD"));
				header.setClaimFromDate(rs1.getString("Clm_From_DT"));
				header.setClaimToDate(rs1.getString("Clm_Through_DT"));
				header.setClaimCategory(rs1.getString("Claim_Type"));
				header.setClaimCurrentStatus(rs1.getString("claim_status"));
				header.setBilledAmt(rs1.getString("Billed_Amount"));
				header.setDrg(rs1.getString("DRG"));
				header.setEmployeeGroup(rs1.getString("Employer_Group"));
				header.setHealthPlan(rs1.getString("Health_Plan"));
				header.setInsuradId(rs1.getString("Insured_ID"));
				header.setNpi(rs1.getString("NPI"));
				header.setAllowedAmt(rs1.getDouble("ML_Predicted_Allowed_Amount"));
				header.setProduct(rs1.getString("Product"));
				header.setDispositionStatus(rs1.getString("Disposition_Status"));
				header.setAuditStatus(rs1.getString("audit_status"));
				header.setPredictionStatus(rs1.getString("prediction_status"));
				header.setLockedBy(rs1.getString("locked_by"));
				header.setDepartmentName(rs1.getString("department_name"));
				header.setFinalAmt(rs1.getDouble("Final_Allowed_Amount"));
				list.add(header);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();
		}
		return list;
	}

	public static boolean UpdateFinalAmount(ClaimHeader claimheader) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			conn = ConnectionUtil.getConnection();
			String query = "update hs_claim_header set Final_Allowed_Amount=? where clm_id=?";
			pstmt = conn.prepareStatement(query);
			pstmt.setDouble(1, claimheader.getFinalAmt());
			pstmt.setString(2, claimheader.getClaimId());

			pstmt.executeUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();
		}
		return true;
	}

	public static List<UserPermissions> assignedClaimsInfo(UserModel request) throws SQLException {
		List<UserPermissions> permissionlist = new ArrayList<UserPermissions>();
		UserPermissions permission = new UserPermissions();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionUtil.getConnection();

			String query = "select * from hs_user_workqueue where userid=? and active='Y'";
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, request.getUserId());
			rs = pstmt.executeQuery();
			while (rs.next()) {
				permission = new UserPermissions();
				permission.setEmpgrp(rs.getString("Employer_Group"));
				permission.setHealthplan(rs.getString("Health_Plan"));
				permission.setProduct(rs.getString("Product"));
				permission.setStatus(rs.getString("status"));
				permissionlist.add(permission);

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();
		}
		return permissionlist;
	}

	public static ClaimInfoResponse getMyworkClaimInfo(UserModel model) throws SQLException {
		List<ClaimLine> claimlist = new ArrayList<ClaimLine>();
		ClaimInfoResponse response = new ClaimInfoResponse();
		ClaimHeader claimheader = new ClaimHeader();
		ClaimLine claim = null;

		List<UserPermissions> permissionlist = new ArrayList<UserPermissions>();
		UserPermissions permission = new UserPermissions();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		PreparedStatement pstmt2 = null;
		ResultSet rs2 = null;
		try {
			conn = ConnectionUtil.getConnection();
			String query2 = "select * from hs_user_workqueue where userid=? and active='Y'";
			pstmt2 = conn.prepareStatement(query2);
			pstmt2.setInt(1, model.getUserId());
			rs2 = pstmt2.executeQuery();
			while (rs2.next()) {
				permission = new UserPermissions();
				permission.setEmpgrp(rs2.getString("Employer_Group"));
				permission.setHealthplan(rs2.getString("Health_Plan"));
				permission.setProduct(rs2.getString("Product"));
				permission.setStatus(rs2.getString("status"));
				permissionlist.add(permission);
				claimheader = new ClaimHeader();
				claimheader = getMyworkClaimHeader(permission);

				if (claimheader != null && claimheader.getClaimId() != null)
					break;
				else
					continue;
			}

			String query = "SELECT * FROM hs_claim_lines WHERE source_clm_id=?  ORDER BY claimline_group_id,record_type";

			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, claimheader.getClaimId());
			rs = pstmt.executeQuery();
			while (rs.next()) {
				claim = new ClaimLine();
				claim.setClaimId(rs.getString("Clm_ID"));
				claim.setClientCd(rs.getString("Client_CD"));
				claim.setClaimLineNo(rs.getString("Clmline_id"));
				claim.setClaimLineGroupID(rs.getInt("claimline_group_id"));
				claim.setDosFrom(rs.getString("Service_From"));
				claim.setDosTo(rs.getString("Service_TO"));
				claim.setPlaceOfService(rs.getString("Place_Of_Service"));
				claim.setHCPC_CPT(rs.getString("HCPC_CPT"));
				claim.setModifierCode(rs.getString("Modifier_Code"));
				claim.setUnits(rs.getString("Final_Allowed_Units"));
				claim.setBilledCharges(rs.getString("Billed_Charges"));
				claim.setRenderingProviderNPI(rs.getString("Rendering_Provider_NPI"));
				claim.setRevenueCode(rs.getString("Revenue_Code"));
				claim.setLineCurrentStatus(rs.getString("line_current_status"));
				claim.setPredictionStatus(rs.getString("prediction_status"));
				claim.setMlPredictedAllowedAmt(rs.getString("Ml_Predicted_Allowed_Amt"));
				claim.setCreatedBy(rs.getString("Created_By"));
				claim.setFinalAmount(rs.getString("Final_Allowed_Amt"));
				claim.setCreatedOn(rs.getString("Created_On"));
				claim.setRemarks(rs.getString("comments"));
				claim.setRecordType(rs.getString("record_type"));

				claimlist.add(claim);
			}

			response.setClaimlinelist(claimlist);
			response.setClaimheader(claimheader);
		} catch (Exception ex) {
			response.setError(ex.getMessage());
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();

		}
		return response;
	}

	public static ClaimHeader getMyworkClaimHeader(UserPermissions permission) throws SQLException {
		System.out.println("JSON" + new Gson().toJson(permission));
		ClaimHeader claimheader = new ClaimHeader();
		Connection conn = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
		try {
			conn = ConnectionUtil.getConnection();
			String query1 = "select * from hs_claim_header where health_plan=? and employer_group=? and product=? and claim_status=?";
			query1 = query1 + " order by created_on asc limit 1";
			System.out.println(" ** MY WORK QUEUE QUERY IN **getMyworkClaimHeader***"+query1);
			pstmt1 = conn.prepareStatement(query1);
			pstmt1.setString(1, permission.getHealthplan());
			pstmt1.setString(2, permission.getEmpgrp());
			pstmt1.setString(3, permission.getProduct());
			pstmt1.setString(4, permission.getStatus());
			rs1 = pstmt1.executeQuery();
			while (rs1.next()) {
				claimheader.setClaimId(rs1.getString("Clm_ID"));
				claimheader.setAkid(rs1.getString("AKID"));
				claimheader.setClientCd(rs1.getString("Client_CD"));
				claimheader.setClaimFromDate(rs1.getString("Clm_From_DT"));
				claimheader.setClaimToDate(rs1.getString("Clm_Through_DT"));
				claimheader.setClaimCategory(rs1.getString("claim_Type"));
				claimheader.setClaimCurrentStatus(rs1.getString("claim_status"));
				claimheader.setBilledAmt(rs1.getString("Billed_Amount"));
				claimheader.setDrg(rs1.getString("DRG"));
				claimheader.setEmployeeGroup(rs1.getString("Employer_Group"));
				claimheader.setHealthPlan(rs1.getString("Health_Plan"));
				claimheader.setInsuradId(rs1.getString("Insured_ID"));
				claimheader.setNpi(rs1.getString("NPI"));
				claimheader.setAllowedAmt(rs1.getDouble("ML_Predicted_Allowed_Amount"));
				claimheader.setProduct(rs1.getString("Product"));
				claimheader.setAuditStatus(rs1.getString("audit_status"));
				claimheader.setPredictionStatus(rs1.getString("Prediction_status"));
				claimheader.setLockedBy(rs1.getString("locked_by"));
				claimheader.setDispositionStatus(rs1.getString("Disposition_Status"));
				claimheader.setCreatedOn(rs1.getString("Created_On"));
				claimheader.setLockedBy(rs1.getString("locked_by"));
				claimheader.setFinalAmt(rs1.getDouble("Final_Allowed_Amount"));
			}
		} catch (Exception ex) {

			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt1 != null)
				pstmt1.close();

		}
		return claimheader;
	}

	public static List<ClaimHistory> getHistory(ClaimHeader request) throws SQLException {
		ClaimLine claimline = null;
		List<ClaimHistory> hislist = new ArrayList<ClaimHistory>();
		ClaimHistory historyobj = null;
		Connection conn = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs = null;
		try {
			String query = "select * from hs_claim_lines_history where claim_id=?";
			conn = ConnectionUtil.getConnection();
			pstmt1 = conn.prepareStatement(query);
			pstmt1.setString(1, request.getClaimId());
			rs = pstmt1.executeQuery();
			while (rs.next()) {
				claimline = new ClaimLine();
				historyobj = new ClaimHistory();
				claimline = new Gson().fromJson(rs.getString("claim_info_json"), ClaimLine.class);
				historyobj.setClaimLine(claimline);
				historyobj.setClaimId(rs.getString("claim_id"));
				historyobj.setCreatedOn(rs.getString("created_date"));
				historyobj.setEditedBy(rs.getString("edited_by"));
				hislist.add(historyobj);
			}
		} catch (Exception ex) {

			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt1 != null)
				pstmt1.close();

		}
		return hislist;
	}

	public static ClaimMoreDetails getMoreClaimInfo(ClaimLine request) throws SQLException {
		// ClaimBO claimbo=new ClaimBO();
		ClaimMoreDetails moredetails = new ClaimMoreDetails();
		Connection conn = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs = null;
		try {
			String query = "select * from hs_claim_lines where clm_id=? and clmline_id=?";
			conn = ConnectionUtil.getConnection();
			pstmt1 = conn.prepareStatement(query);
			pstmt1.setString(1, request.getClaimId());
			pstmt1.setString(2, request.getClaimLineNo());
			rs = pstmt1.executeQuery();
			while (rs.next()) {
				// claimbo=new Gson().fromJson(rs.getString("claim_json"),
				// ClaimBO.class);
				moredetails = new Gson().fromJson(rs.getString("claim_json"), ClaimMoreDetails.class);
			}
		} catch (Exception ex) {

			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt1 != null)
				pstmt1.close();

		}
		return moredetails;
	}

	public static String buildQuery(String healthplan, String product, String empgroup, int userId)
			throws SQLException {
		UserPermissions permission = new UserPermissions();
		List<UserPermissions> permissionlist = new ArrayList<UserPermissions>();
		ResultSet rs2 = null;
		String queue = "";
		PreparedStatement pstmt2 = null;
		Connection conn = null;
		try {
			System.out.println(product);
			String query2 = "select * from hs_user_workqueue where userid=? and active='Y'";
			if (product != null && !product.equalsIgnoreCase("All"))
				query2 = query2 + " and product='" + product + "'";
			if (empgroup != null && !empgroup.equalsIgnoreCase("All"))
				query2 = query2 + " and employer_group='" + empgroup + "'";
			if (healthplan != null && !healthplan.equalsIgnoreCase("All"))
				query2 = query2 + " and health_plan='" + healthplan + "'";
			System.out.println("query2" + query2);
			conn = ConnectionUtil.getConnection();
			pstmt2 = conn.prepareStatement(query2);
			pstmt2.setInt(1, userId);
			rs2 = pstmt2.executeQuery();
			while (rs2.next()) {
				permission = new UserPermissions();
				permission.setEmpgrp(rs2.getString("Employer_Group"));
				permission.setHealthplan(rs2.getString("Health_Plan"));
				permission.setProduct(rs2.getString("Product"));
				permission.setStatus(rs2.getString("status"));
				permission.setClientCd(rs2.getString("client_cd"));
				permissionlist.add(permission);

			}
			if (permissionlist != null) {

				for (int i = 0; i < permissionlist.size(); i++) {
					if (i == 0)
						queue = queue + "(";
					else
						queue = queue + " OR (";
					if (permissionlist.get(i).getHealthplan() != null) {
						queue = queue + " health_plan='" + permissionlist.get(i).getHealthplan() + "'";
					}
					if (permissionlist.get(i).getProduct() != null) {
						queue = queue + " and product='" + permissionlist.get(i).getProduct() + "'";
					}
					if (permissionlist.get(i).getEmpgrp() != null) {
						queue = queue + " and client_cd='" + permissionlist.get(i).getClientCd() + "'";
					}
					if (permissionlist.get(i).getEmpgrp() != null) {
						queue = queue + " and employer_group='" + permissionlist.get(i).getEmpgrp() + "')";
					}

				}

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt2 != null)
				pstmt2.close();
		}
		return queue;
	}

	public static String buildTraningQuery(String healthplan, String product, String empgroup, int userId)
			throws SQLException {
		UserPermissions permission = new UserPermissions();
		List<UserPermissions> permissionlist = new ArrayList<UserPermissions>();
		ResultSet rs2 = null;
		String queue = "";
		PreparedStatement pstmt2 = null;
		Connection conn = null;
		try {
			System.out.println(product);
			String query2 = "select * from hs_user_workqueue where userid=? and active='Y'";
			if (product != null && !product.equalsIgnoreCase("All"))
				query2 = query2 + " and product='" + product + "'";
			if (empgroup != null && !empgroup.equalsIgnoreCase("All"))
				query2 = query2 + " and employer_group='" + empgroup + "'";
			if (healthplan != null && !healthplan.equalsIgnoreCase("All"))
				query2 = query2 + " and health_plan='" + healthplan + "'";
			System.out.println("query2" + query2);
			conn = ConnectionUtil.getConnection();
			pstmt2 = conn.prepareStatement(query2);
			pstmt2.setInt(1, userId);
			rs2 = pstmt2.executeQuery();
			while (rs2.next()) {
				permission = new UserPermissions();
				permission.setEmpgrp(rs2.getString("Employer_Group"));
				permission.setHealthplan(rs2.getString("Health_Plan"));
				permission.setProduct(rs2.getString("Product"));
				permission.setStatus(rs2.getString("status"));
				permission.setClientCd(rs2.getString("client_cd"));
				permissionlist.add(permission);

			}
			if (permissionlist != null) {

				for (int i = 0; i < permissionlist.size(); i++) {
					if (i == 0)
						queue = queue + "(";
					else
						queue = queue + " OR (";
					if (permissionlist.get(i).getHealthplan() != null) {
						queue = queue + " health_plan='" + permissionlist.get(i).getHealthplan() + "'";
					}
					if (permissionlist.get(i).getProduct() != null) {
						queue = queue + " and product='" + permissionlist.get(i).getProduct() + "'";
					}
					// if(permissionlist.get(i).getEmpgrp()!=null)
					// {
					// queue=queue+" and
					// client_cd='"+permissionlist.get(i).getClientCd()+"'";
					// }
					if (permissionlist.get(i).getEmpgrp() != null) {
						queue = queue + " and employer_group='" + permissionlist.get(i).getEmpgrp() + "')";
					}

				}

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt2 != null)
				pstmt2.close();
		}
		return queue;
	}

	public static List<MatchingClaimLine> getClaimsMatchedLines(ClaimHeader request) throws SQLException {
		List<MatchingClaimLine> claimlinelist = new ArrayList<MatchingClaimLine>();
		MatchingClaimLine line = null;
		ResultSet rs2 = null;
		PreparedStatement pstmt2 = null;
		Connection conn = null;

		try {
			conn = ConnectionUtil.getConnection();

			String query = "select * from hs_claim_line_matches where source_clm_id = ? ";

			pstmt2 = conn.prepareStatement(query);
			pstmt2.setString(1, request.getClaimId());
			rs2 = pstmt2.executeQuery();
			while (rs2.next()) {
				line = new MatchingClaimLine();
				line.setBatchid(rs2.getString("batch_id"));
				line.setClaimcategory(rs2.getString("claim_Type"));
				line.setClientcd(rs2.getString("client_cd"));
				line.setCreatedon(rs2.getString("created_on"));
				line.setDetailjson(rs2.getString("detail_json"));
				line.setEmployergroup(rs2.getString("employer_group"));
				line.setHealthplan(rs2.getString("health_plan"));
				line.setInputclaimfeatures(rs2.getString("input_claim_features"));
				line.setMatchedclmid(rs2.getString("matched_clm_id"));
				line.setMatchedclmline(rs2.getString("matched_clm_line"));
				line.setMatchedgroupfeatures(rs2.getString("matched_group_features"));
				line.setMatchedgroupnumber(rs2.getString("matched_group_number"));
				line.setMldistance(rs2.getString("mldistance"));
				line.setMlneighbor(rs2.getString("mlneighbor"));
				line.setModifier1matched(rs2.getString("modifier1_matched"));
				line.setModifier2matched(rs2.getString("modifier2_matched"));
				//line.setOldrategroupmatched(rs2.getString("old_rategroup_matched"));
				line.setPosmatched(rs2.getString("pos_matched"));
				line.setPredictedrate(rs2.getString("predicted_rate"));
				line.setPredictionaccuracy(rs2.getString("prediction_accuracy"));
				line.setPredictionstatus(rs2.getString("prediction_status"));
				line.setPredictionvariation(rs2.getString("prediction_variation"));
				line.setPredictionvariationpct(rs2.getString("prediction_variation_pct"));
				line.setProccodematched(rs2.getString("proccode_matched"));
				line.setProduct(rs2.getString("product"));
				line.setProductmatched(rs2.getString("product_matched"));
				line.setProvidermatched(rs2.getString("provider_matched"));
				line.setSourceclmid(rs2.getString("source_clm_id"));
				line.setSourceclmline(rs2.getString("source_clm_line"));
				line.setSourcerate(rs2.getString("source_rate"));
				line.setRowid(rs2.getString("rowid"));

				claimlinelist.add(line);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt2 != null)
				pstmt2.close();
		}
		return claimlinelist;
	}

	public static List<PredictionResult> newPredictionResults() throws SQLException {
		List<PredictionResult> predictionresultlist = new ArrayList<PredictionResult>();
		ResultSet rs = null;
		PreparedStatement pstmt = null;

		Connection conn = null;
		PredictionResult result = null;
		try {
			String query1 = "SELECT batch_id,prediction_status,prediction_accuracy,COUNT(*)"
					+ " FROM hs_claim_line_matches GROUP BY batch_id,prediction_status,prediction_accuracy"
					+ " ORDER BY batch_id,prediction_status,prediction_accuracy";
			conn = ConnectionUtil.getConnection();
			pstmt = conn.prepareStatement(query1);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				result = new PredictionResult();
				result.setBatchid(rs.getString("batch_id"));
				result.setPredictionStatus(rs.getString("prediction_status"));
				result.setPredictionAccuracy(rs.getString("prediction_accuracy"));
				result.setCount(rs.getString("count(*)"));
				predictionresultlist.add(result);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();

		}
		return predictionresultlist;
	}

	public static List<MLResults> newPredictionResultDetails(PredictionResult request) throws SQLException {
		List<MLResults> predictionresultlist = new ArrayList<MLResults>();
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		Connection conn = null;
		MLResults result = null;
		int count = 0;
		try {
			System.out.println(new Gson().toJson(request));
			String query1 = "SELECT source_clm_id,source_clm_line,matched_group_number,prediction_status,prediction_accuracy,source_rate,predicted_rate,mldistance"
					+ " FROM hs_claim_line_matches  ";
			if (request.getPredictionStatus() != null && !request.getPredictionStatus().equalsIgnoreCase("All")) {
				query1 = query1 + " where prediction_status='" + request.getPredictionStatus() + "'";
				count++;
			}
			if (request.getPredictionAccuracy() != null && !request.getPredictionAccuracy().equalsIgnoreCase("All")) {
				if (count > 0)
					query1 = query1 + " and prediction_accuracy='" + request.getPredictionAccuracy() + "'";
				else
					query1 = query1 + " where prediction_accuracy='" + request.getPredictionAccuracy() + "'";
				count++;
			}

			if (request.getBatchid() != null && !request.getBatchid().equalsIgnoreCase("All")) {
				if (count > 0)
					query1 = query1 + " and batch_id='" + request.getBatchid() + "'";
				else
					query1 = query1 + " where batch_id='" + request.getBatchid() + "'";
				count++;
			}
			query1=query1+" limit 200";
			System.out.println("PredictionSummaryQuery:::" + query1);
			conn = ConnectionUtil.getConnection();
			pstmt = conn.prepareStatement(query1);

			rs = pstmt.executeQuery();
			while (rs.next()) {
				result = new MLResults();
				result.setSourceclmid(rs.getString("source_clm_id"));
				result.setSourceclmline(rs.getString("source_clm_line"));
				result.setMatchGroupId(rs.getString("matched_group_number"));
				result.setPredictionStatus(rs.getString("prediction_status"));
				result.setPredictionType(rs.getString("prediction_accuracy"));
				result.setSourceRate(rs.getString("source_rate"));
				result.setPredictionRate(rs.getString("predicted_rate"));
				result.setDistance(rs.getString("mldistance"));
				result.setPredictionErrorPct(rs.getString("source_rate"), rs.getString("predicted_rate"));
				predictionresultlist.add(result);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();

		}
		return predictionresultlist;
	}

	public static PredictionResultsResponse newPredictionInfo(MLResults request) throws SQLException {
		PredictionResultsResponse response = new PredictionResultsResponse();
		MLMatchClaimLine incomingData = new MLMatchClaimLine();
		MLMatchClaimLine matchedData = new MLMatchClaimLine();
		List<MLMatchClaimLine> mlresponse = new ArrayList<MLMatchClaimLine>();
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		Connection conn = null;

		try {
			String query1 = "SELECT * " + " FROM hs_claim_line_matches" + " WHERE source_clm_id = ?"
					+ " AND source_clm_line = ?";
			conn = ConnectionUtil.getConnection();
			pstmt = conn.prepareStatement(query1);
			pstmt.setString(1, request.getSourceclmid());
			pstmt.setString(2, request.getSourceclmline());
			rs = pstmt.executeQuery();
			while (rs.next()) {
				incomingData = new MLMatchClaimLine();
				incomingData.setSourceClmId(rs.getString("source_clm_id"));
				incomingData.setSourceClmline(rs.getString("source_clm_line"));
				incomingData.setSourcerate(rs.getString("source_rate"));
				incomingData.setPredictedrate(rs.getString("predicted_rate"));
				incomingData.setPredictionstatus(rs.getString("prediction_status"));
				incomingData.setPredictionaccuracy(rs.getString("prediction_accuracy"));
				incomingData.setPredictionVariation(rs.getBigDecimal("prediction_variation"));
				incomingData.setPredictionErrorPct(rs.getBigDecimal("prediction_variation_pct"));
				String sourceRecordString = rs.getString("input_claim_features");
				MLFeatureRecord sourceRecord = gson.fromJson(sourceRecordString, MLFeatureRecord.class);
				incomingData.setSourceProvider(sourceRecord.getProviderid());
				incomingData.setSourcepos(sourceRecord.getPosid());
				incomingData.setSourceproduct(sourceRecord.getProduct());
				incomingData.setSourceproccode(sourceRecord.getProccode());
				incomingData.setSourcemodifier1(sourceRecord.getModifier1());
				incomingData.setSourcemodifier2(sourceRecord.getModifier2());

				matchedData = new MLMatchClaimLine();
				matchedData.setMatchedClmid(rs.getString("matched_clm_id"));
				matchedData.setMatchedClmline(rs.getString("matched_clm_line"));
				String matchRecordString = rs.getString("matched_group_features");
				MLFeatureRecord matchedRecord = gson.fromJson(matchRecordString, MLFeatureRecord.class);
				matchedData.setMatchedProvider(matchedRecord.getProviderid());
				matchedData.setMatchedpos(matchedRecord.getPosid());
				matchedData.setMatchedproduct(matchedRecord.getProduct());
				matchedData.setMatchedproccode(matchedRecord.getProccode());
				matchedData.setMatchedmodifier1(matchedRecord.getModifier1());
				matchedData.setMatchedmodifier2(matchedRecord.getModifier2());
				matchedData.setMatchedrate(matchedRecord.getRate());

				matchedData.setIsprovider(rs.getString("provider_matched").toUpperCase());
				matchedData.setIspos(rs.getString("pos_matched").toUpperCase());
				matchedData.setIsproduct(rs.getString("product_matched").toUpperCase());
				matchedData.setIsproccode(rs.getString("proccode_matched").toUpperCase());
				matchedData.setIsmodifier1(rs.getString("modifier1_matched").toUpperCase());
				matchedData.setIsmodifier2(rs.getString("modifier2_matched").toUpperCase());
				matchedData.setIsrate(rs.getString("modifier2_matched").toUpperCase());

			}
			response.setIncomingData(incomingData);
			mlresponse.add(matchedData);
			response.setMlresponse(mlresponse);
			System.out.println(new Gson().toJson(incomingData));
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();

		}
		return response;
	}

	public static List<MLRunStats> getMLRunStat() throws SQLException {
		List<MLRunStats> statlist = new ArrayList<MLRunStats>();
		MLRunStats stat = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			String query1 = "select * from ml_run_stats";
			conn = ConnectionUtil.getConnection();
			pstmt = conn.prepareStatement(query1);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				stat = new MLRunStats();
				stat.setClientCd(rs.getString("client_cd"));
				//stat.setEmpGrp(rs.getString("employer_group"));
				stat.setHealthPlan(rs.getString("health_plan"));
				//stat.setProdcut(rs.getString("product"));
				stat.setRecentActivity(rs.getString("recent_activity"));
				stat.setBatchid(rs.getString("batch_id"));
				statlist.add(stat);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();

		}
		return statlist;
	}

	public static PredictionResultsResponse getBuildPredictionChart(PredictionResultsResponse response)
			throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
		try {
			StringBuilder predaccuracybuild = new StringBuilder();
			StringBuilder predstatusbuild = new StringBuilder();
			String query = "select prediction_accuracy,count(*) from hs_claim_line_matches group by prediction_accuracy";
			conn = ConnectionUtil.getConnection();
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				predaccuracybuild.append("[\"");
				predaccuracybuild.append(rs.getString("prediction_accuracy") + "\"," + rs.getString("count(*)"));
				predaccuracybuild.append("],");

			}
			String query1 = "select prediction_status,count(*) from hs_claim_line_matches group by prediction_status";
			pstmt1 = conn.prepareStatement(query1);
			rs1 = pstmt1.executeQuery();
			while (rs1.next()) {
				predstatusbuild.append("[\"");
				predstatusbuild.append(rs1.getString("prediction_status") + "\"," + rs1.getString("count(*)"));
				predstatusbuild.append("],");

			}

			String value = predaccuracybuild.toString();
			if (value != null && value.trim().length() > 0)
				response.setPredaccuracy("[" + value.substring(0, value.length() - 1) + "]");

			String value1 = predstatusbuild.toString();
			if (value1 != null && value1.trim().length() > 0)
				response.setPredtype("[" + value1.substring(0, value1.length() - 1) + "]");

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();
			if (pstmt1 != null)
				pstmt1.close();

		}
		return response;
	}

	public static PredictionResultsResponse getBuildSelectedPredictionChart(PredictionResultsResponse response,
			MLRunStats request) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
		int count = 0;
		try {
			StringBuilder predaccuracybuild = new StringBuilder();
			StringBuilder predstatusbuild = new StringBuilder();
			String query = "select prediction_accuracy,count(*) from hs_claim_line_matches ";
			if (request.getBatchid() != null && !request.getBatchid().equalsIgnoreCase("All")) {
				query = query + " where batch_id='" + request.getBatchid() + "'";
				count++;
			}
			if (request.getPredictionAccuracy() != null && !request.getPredictionAccuracy().equalsIgnoreCase("All")) {
				if (count > 0)
					query = query + " and prediction_accuracy='" + request.getPredictionAccuracy() + "'";
				else
					query = query + " where prediction_accuracy='" + request.getPredictionAccuracy() + "'";
				count++;
			}
			if (request.getPredictionStatus() != null && !request.getPredictionStatus().equalsIgnoreCase("All")) {
				if (count > 0)
					query = query + " and prediction_status='" + request.getPredictionStatus() + "'";
				else
					query = query + " where prediction_status='" + request.getPredictionStatus() + "'";
				count++;
			}

			query = query + "group by prediction_accuracy";
			System.out.println(query);
			conn = ConnectionUtil.getConnection();
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				predaccuracybuild.append("[\"");
				predaccuracybuild.append(rs.getString("prediction_accuracy") + "\"," + rs.getString("count(*)"));
				predaccuracybuild.append("],");

			}
			count = 0;
			String query1 = "select prediction_status,count(*) from hs_claim_line_matches ";
			if (request.getBatchid() != null && !request.getBatchid().equalsIgnoreCase("All")) {
				query1 = query1 + " where batch_id='" + request.getBatchid() + "'";
				count++;
			}
			if (request.getPredictionStatus() != null && !request.getPredictionStatus().equalsIgnoreCase("All")) {
				if (count > 0)
					query1 = query1 + " and prediction_status='" + request.getPredictionStatus() + "'";
				else
					query1 = query1 + " where prediction_status='" + request.getPredictionStatus() + "'";
				count++;
			}
			if (request.getPredictionAccuracy() != null && !request.getPredictionAccuracy().equalsIgnoreCase("All")) {
				if (count > 0)
					query1 = query1 + " and prediction_accuracy='" + request.getPredictionAccuracy() + "'";
				else
					query1 = query1 + " where prediction_accuracy='" + request.getPredictionAccuracy() + "'";
				count++;
			}

			query1 = query1 + "group by prediction_status";
			System.out.println(query1);
			pstmt1 = conn.prepareStatement(query1);

			rs1 = pstmt1.executeQuery();

			while (rs1.next()) {
				predstatusbuild.append("[\"");
				predstatusbuild.append(rs1.getString("prediction_status") + "\"," + rs1.getString("count(*)"));
				predstatusbuild.append("],");

			}

			String value = predstatusbuild.toString();
			if (value != null && value.trim().length() > 0)
				response.setPredtype("[" + value.substring(0, value.length() - 1) + "]");

			String value1 = predaccuracybuild.toString();
			if (value1 != null && value1.trim().length() > 0)
				response.setPredaccuracy("[" + value1.substring(0, value1.length() - 1) + "]");

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();
			if (pstmt1 != null)
				pstmt1.close();

		}
		return response;
	}

	public static List<PredictionStat> predcitionStat(String clientcd) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<PredictionStat> predictionstatlist = new ArrayList<PredictionStat>();
		PredictionStat stat = null;
		try {
			conn = ConnectionUtil.getConnection();
			String query = "select * from hs_client_plan_products where client_cd=? and Active_status='Y'";
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, clientcd);
			rs = pstmt.executeQuery();
			System.out.println("**clientcd**" + clientcd);
			while (rs.next()) {
				stat = new PredictionStat();
				stat.setEmployerGroup(rs.getString("employer_group"));
				stat.setHealthPlan(rs.getString("health_plan"));
				stat.setProduct(rs.getString("product"));
				stat.setClientcd(rs.getString("client_cd"));

				PredictionAccuracy accuracy = new PredictionAccuracy();
				accuracy = setDefaultvalues(
						new Gson().fromJson(rs.getString("claim_review_config"), PredictionAccuracy.class));
				stat.setReviewjsonconfig(accuracy);
				PredictionAccuracy accuracy1 = new PredictionAccuracy();
				accuracy1 = setDefaultvalues(
						new Gson().fromJson(rs.getString("claim_audit_config"), PredictionAccuracy.class));
				stat.setAuditjsonconfig(accuracy1);
				predictionstatlist.add(stat);
			}
			System.out.println("**predictionstatlist**" + new Gson().toJson(predictionstatlist));

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();

		}
		return predictionstatlist;
	}

	private static PredictionAccuracy setDefaultvalues(PredictionAccuracy accuracy) {

		if (accuracy == null) {
			accuracy = new PredictionAccuracy();
			accuracy.setNoMatch("0");
			accuracy.setExactMatch("0");
			accuracy.setNearMatch("0");
		} else {
			if (accuracy.getExactMatch() == null || accuracy.getExactMatch().trim().length() <= 0)
				accuracy.setExactMatch("0");
			if (accuracy.getNearMatch() == null || accuracy.getNearMatch().trim().length() <= 0)
				accuracy.setNearMatch("0");

			if (accuracy.getNoMatch() == null || accuracy.getNoMatch().trim().length() <= 0)
				accuracy.setNoMatch("0");

		}
		return accuracy;
	}

	public static boolean submitPredcitionStat(PredictionStat request) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;

		boolean val = false;
		int count = 0;
		try {
			conn = ConnectionUtil.getConnection();
			String query = "update  hs_client_plan_products set claim_review_config=?,claim_audit_config=? where health_plan=? and product=? and employer_group=? and client_cd=?";
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, new Gson().toJson(request.getReviewjsonconfig()));
			pstmt.setString(2, new Gson().toJson(request.getAuditjsonconfig()));
			pstmt.setString(3, request.getHealthPlan());
			pstmt.setString(4, request.getProduct());
			pstmt.setString(5, request.getEmployerGroup());
			pstmt.setString(6, request.getClientcd());
			count = pstmt.executeUpdate();
			if (count > 0)
				val = true;
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();

		}
		return val;
	}

	public static String checkIsAudit(ClaimInfoResponse request) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String value = null;
		try {
			conn = ConnectionUtil.getConnection();
			String query = "select is_auto_audited from hs_claim_header where clm_id=?";
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, request.getClaimheader().getClaimId());
			rs = pstmt.executeQuery();
			while (rs.next()) {
				value = rs.getString("is_auto_audited");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();

		}
		return value;
	}

	public static List<ClaimLineInfo> processClaimLine(ClaimHeader request) throws SQLException {
		List<ClaimLineInfo> claimlineinfolist = new ArrayList<ClaimLineInfo>();
		ClaimLineInfo info = new ClaimLineInfo();
		ClaimLine claim = new ClaimLine();
		List<ClaimLine> claimlineList = new ArrayList<ClaimLine>();
		int linegroupId = 0;
		int linecount = 0;
		
		String query = "SELECT * FROM hs_claim_lines WHERE source_clm_id=?  ORDER BY claimline_group_id, record_type ";
		if(request.getClaimCurrentStatus().trim().equalsIgnoreCase("Released"))
			 query = "SELECT * FROM hs_claim_lines_released WHERE source_clm_id=?  ORDER BY claimline_group_id, record_type ";
			
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionUtil.getConnection();
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, request.getClaimId());
			rs = pstmt.executeQuery();
			while (rs.next()) {

				if ((!(rs.getInt("claimline_group_id") == linegroupId))) {

					if (linegroupId != 0) {

						info.setCount(linecount);
						info = validateClaimStats(info);
						claimlineinfolist.add(info);
					}
					linecount = 0;
					linegroupId = rs.getInt("claimline_group_id");
					info = new ClaimLineInfo();
					info.setGroupId(linegroupId);
					claimlineList = new ArrayList<ClaimLine>();

				}

				claim = new ClaimLine();
				claim.setClaimId(rs.getString("Clm_ID"));
				claim.setClientCd(rs.getString("Client_CD"));
				claim.setClaimLineNo(rs.getString("Clmline_id"));
				claim.setClaimLineGroupID(rs.getInt("claimline_group_id"));
				claim.setDosFrom(rs.getString("Service_From"));
				claim.setDosTo(rs.getString("Service_TO"));
				claim.setPlaceOfService(rs.getString("Place_Of_Service"));
				claim.setHCPC_CPT(rs.getString("HCPC_CPT"));
				claim.setModifierCode(rs.getString("Modifier_Code"));
				claim.setUnits(rs.getString("Final_Allowed_Units"));
				claim.setBilledCharges(rs.getString("Billed_Charges"));
				claim.setRenderingProviderNPI(rs.getString("Rendering_Provider_NPI"));
				claim.setRevenueCode(rs.getString("Revenue_Code"));
				claim.setLineCurrentStatus(rs.getString("line_current_status"));
				claim.setPredictionStatus(rs.getString("prediction_status"));
				claim.setMlPredictedAllowedAmt(rs.getString("Ml_Predicted_Allowed_Amt"));
				claim.setCreatedBy(rs.getString("Created_By"));
				claim.setFinalAmount(rs.getString("Final_Allowed_Amt"));
				claim.setCreatedOn(rs.getString("Created_On"));
				claim.setRemarks(rs.getString("remarks"));
				claim.setComments(rs.getString("comments"));
				claim.setRecordType(rs.getString("record_type"));
				claimlineList.add(claim);
				info.setClaimLineList(claimlineList);
				linecount++;

			}
			// last one not getting added in the list
			info.setCount(linecount);
			info = validateClaimStats(info);
			claimlineinfolist.add(info);

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();

		}
		return claimlineinfolist;
	}

	private static ClaimLineInfo validateClaimStats(ClaimLineInfo info) {
		String hccpt = null;
		String npi = null;
		String pos = null;
		String mod = null;
		for (int i = 0; i < info.getClaimLineList().size(); i++) {
			if (info.getClaimLineList().get(i).getHCPC_CPT()
					.equalsIgnoreCase(info.getClaimLineList().get(0).getHCPC_CPT()))
				hccpt = "Match";
			else
				hccpt = "No Match";

			if (info.getClaimLineList().get(i).getRenderingProviderNPI()
					.equalsIgnoreCase(info.getClaimLineList().get(0).getRenderingProviderNPI()))
				npi = "Match";
			else
				npi = "No Match";

			if (info.getClaimLineList().get(i).getPlaceOfService()
					.equalsIgnoreCase(info.getClaimLineList().get(0).getPlaceOfService()))
				pos = "Match";
			else
				pos = "No Match";

			if (info.getClaimLineList().get(i).getModifierCode()
					.equalsIgnoreCase(info.getClaimLineList().get(0).getModifierCode()))
				mod = "Match";
			else
				mod = "No Match";

		}
		info.setHccpt(hccpt);
		info.setPos(pos);
		info.setRenderingnpi(npi);
		info.setMod(mod);

		return info;
	}

	public static List<TrainingGroup> trainingGroups(TrainingGroupRequest request) throws SQLException {
		List<TrainingGroup> trainingGroupsList=new ArrayList<TrainingGroup>();
		TrainingGroup group=new TrainingGroup();
		int count=0;
		String query = "SELECT group_number,providerid,siteid,ratecategory,proccode,modifiercd,modifer2,rate,service_date,health_plan,claim_type,client_cd"
						+ " FROM ml_group_recent_record"
						+ " WHERE ";
		
		
		if(request.getProccode()!=null && request.getProviderId()!=null && request.getProccode().trim().length()>0 && request.getProviderId().trim().length()>0)
		{
			query=query+" (providerid ='"+ request.getProviderId()+"' Or proccode='"+request.getProccode()+"')";
			count++;
		}
		else if(request.getProccode()!=null && request.getProccode().trim().length()>0)
		{
			query=query+" proccode='"+request.getProccode()+"'";
			count++;
		}
		else if(request.getProviderId()!=null && request.getProviderId().trim().length()>0)
		{
			query=query+" providerid='"+request.getProviderId()+"'";
			count++;
		}
		
		
		if(request.getHealthplan()!=null && !request.getHealthplan().equalsIgnoreCase("All"))
		{
			if(count>0)
			query=query+" and health_plan='"+request.getHealthplan()+"'";
			else
				query=query+" health_plan='"+request.getHealthplan()+"'";	
			count++;
		}
		if(request.getClaimType()!=null && !request.getClaimType().equalsIgnoreCase("All"))
		{
			if(count>0)
			{
				query=query+" and ";	
			}
			query=query+"claim_type in ('";
			if(request.getClaimType().equalsIgnoreCase("Institution"))
			query=query+request.getClaimType()+"','I')";
			
			else if(request.getClaimType().equalsIgnoreCase("Professional"))
				query=query+request.getClaimType()+"','P')";	
			count++;
		}
		if(request.getClientCode()!=null && !request.getClientCode().equalsIgnoreCase("All"))
		{
			if(count>0)
			query=query+" and client_cd='"+request.getClientCode()+"'";
			else
				query=query+" client_cd='"+request.getClientCode()+"'";	
			count++;
		}
		System.out.println("TraingGrpQuery::"+query);
		
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionUtil.getConnection();
			pstmt = conn.prepareStatement(query);
			rs=pstmt.executeQuery();
			while(rs.next())
			{
				group=new TrainingGroup();
				group.setGroupNumber(rs.getString("group_number"));
				group.setModifer2(rs.getString("modifer2"));
				group.setModifiercd(rs.getString("modifiercd"));
				group.setProccode(rs.getString("proccode"));
				group.setProviderid(rs.getString("providerid"));
				group.setRate(rs.getString("rate"));
				group.setRatecategory(rs.getString("ratecategory"));
				group.setServicedate(rs.getString("service_date"));
				group.setSiteid(rs.getString("siteid"));
				group.setHealthplan(rs.getString("health_plan"));
				group.setClaimType(rs.getString("claim_type"));
				group.setClientCode(rs.getString("client_cd"));
				
				trainingGroupsList.add(group);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();

		}
		return trainingGroupsList;
	}

	public static List<TrainingGroup> similarTrainingGroups(TrainingGroup request) throws SQLException {
		List<TrainingGroup> trainingGroupsList=new ArrayList<TrainingGroup>();
		TrainingGroup group=new TrainingGroup();
		int count=0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			String query="SELECT group_number,providerid,siteid,ratecategory,proccode,modifiercd,modifer2,rate,service_date"
					+ " FROM ml_group_record"
					+ " WHERE providerid=? AND proccode=?"
					+ " AND client_cd=? AND health_plan=?"
					+ " AND claim_Type=? ORDER BY providerid,siteid,ratecategory,proccode,modifiercd,modifer2,rate,service_date";
			conn = ConnectionUtil.getConnection();
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1,request.getProviderid());
			pstmt.setString(2,request.getProccode());
			pstmt.setString(3,request.getClientCode());
			pstmt.setString(4,request.getHealthplan());
			pstmt.setString(5,request.getClaimType());
			rs=pstmt.executeQuery();
			while(rs.next())
			{
				group=new TrainingGroup();
				group.setGroupNumber(rs.getString("group_number"));
				group.setModifer2(rs.getString("modifer2"));
				group.setModifiercd(rs.getString("modifiercd"));
				group.setProccode(rs.getString("proccode"));
				group.setProviderid(rs.getString("providerid"));
				group.setRate(rs.getString("rate"));
				group.setRatecategory(rs.getString("ratecategory"));
				group.setServicedate(rs.getString("service_date"));
				group.setSiteid(rs.getString("siteid"));
				
				trainingGroupsList.add(group);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();

		}
		return trainingGroupsList;
	}

	public static ClaimDetailResponse todayProcessed(UserModel request, ClaimDetailResponse response) throws SQLException {
		int count=0;
		int queue=0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		List<UserPermissions> permissionlist = new ArrayList<UserPermissions>();
		UserPermissions permission = new UserPermissions();
		ResultSet rs1 = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		try 
		{
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd ");
			  LocalDateTime now = LocalDateTime.now();
			String date=dtf.format(now);
			conn=ConnectionUtil.getConnection();
			String query="select distinct claim_id from hs_claim_header_history "
					+ " where edited_by=? and created_date>=? and created_date<=?";
			pstmt=conn.prepareStatement(query);
			pstmt.setInt(1,request.getUserId());
			pstmt.setString(2,date+" 00:00:01");
			pstmt.setString(3,date+" 23:59:59");
			 rs1=pstmt.executeQuery();
			while(rs1.next())
			{
				count++;
			}
			response.setProcessedCount(Integer.toString(count));

			String query2 = "select * from hs_user_workqueue where userid=? and active='Y'";
			pstmt2 = conn.prepareStatement(query2);
			pstmt2.setInt(1, request.getUserId());
			rs2 = pstmt2.executeQuery();
			while (rs2.next()) {
				permission = new UserPermissions();
				permission.setEmpgrp(rs2.getString("Employer_Group"));
				permission.setHealthplan(rs2.getString("Health_Plan"));
				permission.setProduct(rs2.getString("Product"));
				permission.setStatus(rs2.getString("status"));
				permission.setClientCd(rs2.getString("client_cd"));
				permissionlist.add(permission);

			}
			
			String query1="select count(*) from hs_claim_header where (locked_by is null or locked_by=?) and ";
				if (permissionlist != null) {
					query1 = query1 + "(";
					for (int i = 0; i < permissionlist.size(); i++) {

						if (i == 0)
							query1 = query1 + "(";
						else
							query1 = query1 + " OR (";
						if (permissionlist.get(i).getHealthplan() != null) {
							query1 = query1 + " health_plan='" + permissionlist.get(i).getHealthplan() + "'";
						}
						if (permissionlist.get(i).getProduct() != null) {
							query1 = query1 + " and product='" + permissionlist.get(i).getProduct() + "'";
						}
						if (permissionlist.get(i).getStatus() != null) {
							query1 = query1 + " and Claim_Status='" + permissionlist.get(i).getStatus() + "'";
						}
						if (permissionlist.get(i).getEmpgrp() != null) {
							query1 = query1 + " and employer_group='" + permissionlist.get(i).getEmpgrp() + "')";
						}

					}
					query1 = query1 + ")";
				}
				if(permissionlist != null && !permissionlist.isEmpty())
				{
					System.out.println("Quert::"+query1);
					pstmt1 = conn.prepareStatement(query1);
					pstmt1.setInt(1, request.getUserId());
					rs=pstmt1.executeQuery();
					while(rs.next())
					{
							queue=rs.getInt("count(*)");
					}
				}
			
			response.setMyQueueCount(Integer.toString(queue));
			
		}catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();

		}
		return response;
	}

	public static ClaimSummaryResponse getAgeandAmountList(LoginResponse request, ClaimSummaryResponse response) throws SQLException {
		String queue="(";
		Connection conn=null;
		PreparedStatement pstmt=null;
		PreparedStatement pstmt1=null;
		ResultSet rs=null;
		ResultSet rs1=null;
		Set<String> agelist = new HashSet<String>();
		Set<String> amountlist = new HashSet<String>();
		try{
			conn=ConnectionUtil.getConnection();
			if(request.getClaimsassginedlist()!=null)
			{
				
				for(int i=0;i<request.getClaimsassginedlist().size();i++)
				{
					if(i==0)
						queue=queue+"(";
					else
						queue=queue+" OR (";
					if(request.getClaimsassginedlist().get(i).getHealthplan()!=null)
					{
						queue=queue+" health_plan='"+request.getClaimsassginedlist().get(i).getHealthplan()+"'";
					}
					if(request.getClaimsassginedlist().get(i).getProduct()!=null)
					{
						queue=queue+" and product='"+request.getClaimsassginedlist().get(i).getProduct()+"'";
					}
					if(request.getClaimsassginedlist().get(i).getEmpgrp()!=null)
					{
						queue=queue+" and client_cd='"+request.getClaimsassginedlist().get(i).getClientCd()+"'";
					}
					if(request.getClaimsassginedlist().get(i).getEmpgrp()!=null)
					{
						queue=queue+" and employer_group='"+request.getClaimsassginedlist().get(i).getEmpgrp()+"')";
					}
					
				}
				
			}
			
			queue=queue+")";
			String query="SELECT b.display_name,SUM(a.countage) FROM "
					+ " (SELECT COUNT(*) AS countage, CASE  WHEN DATEDIFF(CURDATE(),created_On) <= 1  THEN 1 "
					+ " WHEN DATEDIFF(CURDATE(),created_On) > 1 AND DATEDIFF(CURDATE(),created_On) <=3 THEN 2 "
					+ " WHEN DATEDIFF(CURDATE(),created_On) > 3 AND DATEDIFF(CURDATE(),created_On) <=7 THEN 3 "
					+ " WHEN DATEDIFF(CURDATE(),created_On) > 7 AND DATEDIFF(CURDATE(),created_On) <=30 THEN 4 "
					+ " ELSE 5 END AS Age "
					+ " FROM hs_claim_header where ";
				if(queue.length()>2)
					query=query+queue;
					
					
				query=query+ " GROUP BY CASE  WHEN DATEDIFF(CURDATE(),created_On) <= 1  THEN 1 "
					+ " WHEN DATEDIFF(CURDATE(),created_On) > 1 AND DATEDIFF(CURDATE(),created_On) <=3 THEN 2 "
					+ " WHEN DATEDIFF(CURDATE(),created_On) > 3 AND DATEDIFF(CURDATE(),created_On) <=7 THEN 3 "
					+ " WHEN DATEDIFF(CURDATE(),created_On) > 7 AND DATEDIFF(CURDATE(),created_On) <=30 THEN 4"
					+ " ELSE 5 END) a , hs_dashboard_groups b "
					+ " WHERE b.display_order_id=a.age AND   b.grouping_type='Aging' GROUP BY a.age";
			
				System.out.println("dashboard query 1"+query);
			pstmt=conn.prepareStatement(query);
			rs=pstmt.executeQuery();
			while(rs.next())
			{
				agelist.add(rs.getString("display_name"));
				
			}
			
			String query1="SELECT b.display_name,SUM(a.amount) FROM ("
					+ " SELECT CASE  WHEN Ml_predicted_Allowed_Amount <= 100  THEN 1"
					+ " WHEN Ml_predicted_Allowed_Amount > 100 AND Ml_predicted_Allowed_Amount <= 500 THEN 2"
					+ " WHEN Ml_predicted_Allowed_Amount > 501 AND Ml_predicted_Allowed_Amount <=1000 THEN 3"
					+ " WHEN Ml_predicted_Allowed_Amount > 1000 THEN 4 ELSE 5 END AS Amount"
					+ " FROM hs_claim_header where ";
					query1=query1+queue;
					query1=query1+ " ) a ,hs_dashboard_groups b"
					+ " WHERE b.display_order_id=a.amount AND   b.grouping_type='Amounts'"
					+ " GROUP BY a.amount";
					System.out.println("dashboard query 2"+query1);
			pstmt1=conn.prepareStatement(query1);
			rs1=pstmt1.executeQuery();
			while(rs1.next())
			{
				amountlist.add(rs1.getString("display_name"));	
			}
			response.setAgelist(new ArrayList(agelist));
			response.setAmountlist(new ArrayList(amountlist));
		}catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();

		}
		return response;
	}

	
	
	
	public static List<ClaimHeader> getDashSelectedClaimDetails(ClaimRequest request) throws SQLException {
		List<ClaimHeader> claimlist = new ArrayList<ClaimHeader>();
		ClaimHeader claim = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int count = 0;
		String queue=null;
		try {
			conn = ConnectionUtil.getConnection();
			String query = "SELECT * FROM hs_claim_Header where ";

			if (!request.getPredictionMatch().equalsIgnoreCase("All")) {
				if (count > 0)
					query = query + " and Prediction_Status='" + request.getPredictionMatch() + "'" + " and ";
				else
					query = query + "  Prediction_Status='" + request.getPredictionMatch() + "'" + " and ";
			}
			if (!request.getAuditStatus().equalsIgnoreCase("All")) {
				if (count > 0)
					query = query + " and audit_status='" + request.getAuditStatus() + "'" + " and ";
				else
					query = query + "  audit_status='" + request.getAuditStatus() + "'" + " and ";
			}
			if (!request.getClaimStatus().equalsIgnoreCase("All")) {
				if (count > 0)
					query = query + " and claim_Status='" + request.getClaimStatus() + "'" + " and ";
				else
					query = query + " claim_Status='" + request.getClaimStatus() + "'" + " and ";
			}
			if (!request.getAllowedAmount().equalsIgnoreCase("All")) {

				if (count > 0)
					query = query + " and ";
				
				if(request.getAllowedAmount().equalsIgnoreCase("Less than 100"))
				{
					query=query+"Ml_predicted_Allowed_Amount <= 100 and ";
				}
				else if(request.getAllowedAmount().equalsIgnoreCase("99 to 500"))
				{
					query=query+"Ml_predicted_Allowed_Amount > 100 AND Ml_predicted_Allowed_Amount <= 500 and ";
				}
				else if(request.getAllowedAmount().equalsIgnoreCase("501 to 1000"))
				{
					query=query+"Ml_predicted_Allowed_Amount > 501 AND Ml_predicted_Allowed_Amount <=1000 and ";
				}
				else if(request.getAllowedAmount().equalsIgnoreCase("More than 1000"))
				{
					query=query+"Ml_predicted_Allowed_Amount > 1000 and";
				}
				else if(request.getAllowedAmount().equalsIgnoreCase("Zero or Missing"))
				{
					query=query+"Ml_predicted_Allowed_Amount =0 or Ml_predicted_Allowed_Amount is null and";
				}
				
			}
			
			if (!request.getAge().equalsIgnoreCase("All")) {

				if (count > 0)
					query = query + " and ";
				if(request.getAge().equalsIgnoreCase("8 to 30 Days"))
				{
					query=query+"DATEDIFF(CURDATE(),created_On) > 7 AND DATEDIFF(CURDATE(),created_On) <=30 and ";
				}
				else if(request.getAge().equalsIgnoreCase("4 to 7 Days"))
				{
					query=query+"DATEDIFF(CURDATE(),created_On) > 3 AND DATEDIFF(CURDATE(),created_On) <=7 and ";
				}
				else if(request.getAge().equalsIgnoreCase("1 to 3 Days"))
				{
					query=query+"DATEDIFF(CURDATE(),created_On) > 1 AND DATEDIFF(CURDATE(),created_On) <=3 and ";
				}
				else if(request.getAge().equalsIgnoreCase("Less than a Day"))
				{
					query=query+"DATEDIFF(CURDATE(),created_On) <= 1 and ";
				}
				else if(request.getAge().equalsIgnoreCase("More than 30 Days"))
				{
					query=query+"DATEDIFF(CURDATE(),created_On) >30 and ";
				}
			}

			if (!request.getHealthplanType().equalsIgnoreCase("All")
					|| !request.getProductType().equalsIgnoreCase("All")
					|| !request.getEmpgrpType().equalsIgnoreCase("All"))
				
					queue=JDBClaimUtilMysql.buildQuery(request.getHealthplanType(), request.getProductType(),
								request.getEmpgrpType(), request.getLoginresponse().getModel().getUserId());
						
			else
				queue= JDBClaimUtilMysql.buildQuery(request.getHealthplanType(), request.getProductType(),
								request.getEmpgrpType(), request.getLoginresponse().getModel().getUserId());
			
			
			queue="  ("+queue	+ ")";
			
			if(queue.trim().length()>2)
			query = query +queue;
			
			query=query+" limit 200";
			System.out.println("ClaimDetailQuery::" + query);
			if(queue.trim().length()>2)
			{
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				while (rs.next()) {
				claim = new ClaimHeader();
				claim.setClaimId(rs.getString("Clm_ID"));
				claim.setAkid(rs.getString("AKID"));
				claim.setClientCd(rs.getString("Client_CD"));
				claim.setClaimFromDate(rs.getString("Clm_From_DT"));
				claim.setClaimToDate(rs.getString("Clm_Through_DT"));
				claim.setClaimCategory(rs.getString("Claim_type"));
				claim.setClaimCurrentStatus(rs.getString("claim_status"));
				claim.setPredictionStatus(rs.getString("prediction_status"));
				claim.setAuditStatus(rs.getString("audit_status"));
				claim.setBilledAmt(rs.getString("Billed_Amount"));
				claim.setDrg(rs.getString("DRG"));
				claim.setEmployeeGroup(rs.getString("Employer_Group"));
				claim.setHealthPlan(rs.getString("Health_Plan"));
				claim.setInsuradId(rs.getString("Insured_ID"));
				claim.setNpi(rs.getString("NPI"));
				claim.setAllowedAmt(rs.getDouble("ML_Predicted_Allowed_Amount"));
				claim.setProduct(rs.getString("Product"));
				claim.setDispositionStatus(rs.getString("Disposition_Status"));
				claim.setLockedBy(rs.getString("locked_by"));
				claim.setDepartmentName(rs.getString("department_name"));

				claim.setFinalAmt(rs.getDouble("Final_Allowed_Amount"));
				claimlist.add(claim);
				}
			}

		} catch (Exception ex) {

			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();

		}

		return claimlist;
	}

	
	public static void deleteClaim(String clmID) throws SQLException {
		Connection connection = null;
		try {
			connection = ConnectionUtil.getConnection();
			String deleteHeader = "delete from hs_claim_header where clm_id=" + clmID;
			PreparedStatement pshDelete = connection.prepareStatement(deleteHeader);
			pshDelete.executeUpdate();

			String deleteLine = "delete from hs_claim_lines where source_clm_id=" + clmID;
			PreparedStatement pslDelete = connection.prepareStatement(deleteLine);
			pslDelete.executeUpdate();

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (connection != null)
				connection.close();

		}

	}

	public static List<ClaimHeader> myProcessedClaims(LoginResponse request) throws SQLException {
		List<ClaimHeader> claimlist = new ArrayList<ClaimHeader>();
		List<String> clmids=new ArrayList<String>();
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs1 = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs2 = null;
		ClaimHeader header = new ClaimHeader();
		
		try {
			conn = ConnectionUtil.getConnection();
			String query = "SELECT distinct(claim_id) FROM hs_claim_Header_history WHERE created_date>=? and created_date<=? and edited_by=?";
			String query1="SELECT * FROM hs_claim_Header WHERE clm_id in(";
			String query2="SELECT * FROM hs_claim_Header_released WHERE clm_id in(";
			
			
			
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd ");
			  LocalDateTime now = LocalDateTime.now();
			String date=dtf.format(now);
			
		
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1,date+" 00:00:01");
			pstmt.setString(2,date+" 23:59:59");
			pstmt.setInt(3, request.getModel().getUserId());
			

			System.out.println("myProcessedClaims QUERY::" + query);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				clmids.add(rs.getString("claim_id"));
			}
			if(clmids!=null && !clmids.isEmpty())
			{
				for(int i=0;i<clmids.size();i++)
				{
					query1=query1+clmids.get(i);
					query2=query2+clmids.get(i);
					if(i<clmids.size()-1)
					{
						query1=query1+",";
						query2=query2+",";
					}
					
				}
			}
			query1=query1+")";
			query2=query2+")";
			System.out.println("myProcessedClaims QUERY1::" + query1);
			System.out.println("myProcessedClaims QUERY2::" + query2);
			pstmt1 = conn.prepareStatement(query1);
			pstmt2 = conn.prepareStatement(query2);
			rs1 = pstmt1.executeQuery();
			rs2 = pstmt2.executeQuery();
			
				while (rs1.next()) {
					header = new ClaimHeader();
					header.setClaimId(rs1.getString("clm_id"));
					header.setAkid(rs1.getString("AKID"));
					header.setClientCd(rs1.getString("Client_CD"));
					header.setClaimFromDate(rs1.getString("Clm_From_DT"));
					header.setClaimToDate(rs1.getString("Clm_Through_DT"));
					header.setClaimCategory(rs1.getString("Claim_Type"));
					header.setClaimCurrentStatus(rs1.getString("claim_status"));
					header.setBilledAmt(rs1.getString("Billed_Amount"));
					header.setDrg(rs1.getString("DRG"));
					header.setEmployeeGroup(rs1.getString("Employer_Group"));
					header.setHealthPlan(rs1.getString("Health_Plan"));
					header.setInsuradId(rs1.getString("Insured_ID"));
					header.setNpi(rs1.getString("NPI"));
					header.setAllowedAmt(rs1.getDouble("ML_Predicted_Allowed_Amount"));
					header.setProduct(rs1.getString("Product"));
					header.setDispositionStatus(rs1.getString("Disposition_Status"));
					header.setAuditStatus(rs1.getString("audit_status"));
					header.setPredictionStatus(rs1.getString("prediction_status"));
					header.setLockedBy(rs1.getString("locked_by"));
					header.setDepartmentName(rs1.getString("department_name"));
					header.setFinalAmt(rs1.getDouble("Final_Allowed_Amount"));
					claimlist.add(header);
				
			}
			while(rs2.next())
			{
				header = new ClaimHeader();
				header.setClaimId(rs2.getString("clm_id"));
				header.setAkid(rs2.getString("AKID"));
				header.setClientCd(rs2.getString("Client_CD"));
				header.setClaimFromDate(rs2.getString("Clm_From_DT"));
				header.setClaimToDate(rs2.getString("Clm_Through_DT"));
				header.setClaimCategory(rs2.getString("Claim_Type"));
				header.setClaimCurrentStatus(rs2.getString("claim_status"));
				header.setBilledAmt(rs2.getString("Billed_Amount"));
				header.setDrg(rs2.getString("DRG"));
				header.setEmployeeGroup(rs2.getString("Employer_Group"));
				header.setHealthPlan(rs2.getString("Health_Plan"));
				header.setInsuradId(rs2.getString("Insured_ID"));
				header.setNpi(rs2.getString("NPI"));
				header.setAllowedAmt(rs2.getDouble("ML_Predicted_Allowed_Amount"));
				header.setProduct(rs2.getString("Product"));
				header.setDispositionStatus(rs2.getString("Disposition_Status"));
				header.setAuditStatus(rs2.getString("audit_status"));
				header.setPredictionStatus(rs2.getString("prediction_status"));
				header.setLockedBy(rs2.getString("locked_by"));
				header.setDepartmentName(rs2.getString("department_name"));
				header.setFinalAmt(rs2.getDouble("Final_Allowed_Amount"));
				claimlist.add(header);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();
		}
		return claimlist;
	}


}