package com.pos.services.intf;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Service;

import com.pos.a.model.ClaimDecision;
import com.pos.a.model.ClaimEditRequest;
import com.pos.a.model.ClaimHeader;
import com.pos.a.model.ClaimInfoResponse;
import com.pos.a.model.ClaimLine;
import com.pos.a.model.ClaimLockRequest;
import com.pos.a.model.ClaimRequest;
import com.pos.a.model.ClaimSearchModel;
import com.pos.a.model.ClaimStatus;
import com.pos.a.model.ClaimSummary;
import com.pos.a.model.MLResults;
import com.pos.a.model.MLRunStats;
import com.pos.a.model.PredictionResult;
import com.pos.a.model.PredictionResultsRequest;
import com.pos.a.model.PredictionStat;
import com.pos.a.model.TrainingGroup;
import com.pos.a.model.TrainingGroupRequest;
import com.pos.a.model.UserModel;
import com.pos.login.model.LoginResponse;








@Path("/claimservice/")
@Service
@Consumes("application/json")
@Produces("application/json")
public interface ClaimService {

	@POST
	@Path("/claimlist")
	@Produces("application/json")
	// List for claimlist
	public Response getClaimList(LoginResponse request) throws Exception;
	
	
	
	
	@POST
	@Path("/selectedclaimlist")
	@Produces("application/json")
	// List for selectedclaimlist
	public Response getSelectedClaimList(ClaimRequest request) throws Exception;
	
	
	@POST
	@Path("/claimdetails")
	@Produces("application/json")
	// List for claimlist
	public Response getClaimDetails(ClaimSummary request) throws Exception;
	
	@POST
	@Path("/selectedclaimdetails")
	@Produces("application/json")
	// List for selectedclaimlist
	public Response getSelectedClaimDetails(ClaimRequest request) throws Exception;
	
	@POST
	@Path("/claimInfo")
	@Produces("application/json")
	// List for selectedclaimlist
	public Response getClaimInfo(ClaimHeader request) throws Exception;
	
	@POST
	@Path("/hsSearch")
	@Produces("application/json")
	public Response getMatchSearch(ClaimSearchModel request) throws Exception;
	
	@POST
	@Path("/editInfo")
	@Produces("application/json")
	public Response getClaimInfoForEdit(ClaimHeader request) throws Exception;
	
	@POST
	@Path("/editclaimLine")
	@Produces("application/json")
	public Response editClaimLine(ClaimEditRequest request) throws Exception;
	
	@POST
	@Path("/lockclaim")
	@Produces("application/json")
	public Response lockClaim(ClaimEditRequest request) throws Exception;
	
	
	@POST
	@Path("/submitdecision")
	@Produces("application/json")
	public Response submitDecision(ClaimDecision request) throws Exception;
	
	
	@POST
	@Path("/claimstatus")
	@Produces("application/json")
	public Response claimStatus(ClaimStatus request) throws Exception;
	
	@POST
	@Path("/sendToAudit")
	@Produces("application/json")
	public Response sendToAudit(ClaimInfoResponse request) throws Exception;
	
	@POST
	@Path("/acceptclaimline")
	@Produces("application/json")
	public Response acceptClaimLine(ClaimEditRequest request) throws Exception;
	
	@POST
	@Path("/lockedclaimslist")
	@Produces("application/json")
	public Response lockedClaimsList(ClaimLockRequest request) throws Exception;
	
	@POST
	@Path("/lockedclaimscount")
	@Produces("application/json")
	public Response lockedClaimsCount(UserModel request) throws Exception;
	
	@POST
	@Path("/unlockClaim")
	@Produces("application/json")
	public Response unlockClaim(ClaimLockRequest request) throws Exception;
	
	
	@POST
	@Path("/predictionresults")
	@Produces("application/json")
	public Response predictionResults(PredictionResultsRequest request) throws Exception;
	
	@POST
	@Path("/mltestandtraindata")
	@Produces("application/json")
	public Response mlTestAndTrainData(PredictionResultsRequest request) throws Exception;
	
	
	@POST
	@Path("/predictionreviewsummary")
	@Produces("application/json")
	public Response predictionReviewSummary(PredictionResultsRequest request) throws Exception;
	
	@POST
	@Path("/releaseallclaims")
	@Produces("application/json")
	public Response releaseAllClaims(ClaimSummary request) throws Exception;
	
	@POST
	@Path("/selectedmltestandtraindata")
	@Produces("application/json")
	public Response getSeletcedPredictionList(ClaimRequest request) throws Exception;
	
	@POST
	@Path("/reasonslist")
	@Produces("application/json")
	public Response reasonsList(UserModel request) throws Exception;
	
	@POST
	@Path("/assginedclaimlist")
	@Produces("application/json")
	public Response getmyAssignedClaims(UserModel request) throws Exception;
	
	@POST
	@Path("/assginedclaiminfo")
	@Produces("application/json")
	public Response assignedClaimsInfo(UserModel request) throws Exception;
	
	@POST
	@Path("/moreClaimInfo")
	@Produces("application/json")
	public Response getMoreClaimInfo(ClaimLine request) throws Exception;
	
	@POST
	@Path("/newPredictionResults")
	@Produces("application/json")
	public Response newPredictionResults(UserModel request) throws Exception;
	
	@POST
	@Path("/newPredictionsummary")
	@Produces("application/json")
	public Response newPredictionResultDetails(PredictionResult request) throws Exception;
	
	@POST
	@Path("/newPredictionInfo")
	@Produces("application/json")
	public Response newPredictionInfo(MLResults request) throws Exception;
	
	@POST
	@Path("/selectedPredictionResults")
	@Produces("application/json")
	public Response selectedNewPredcitionResults(MLRunStats request) throws Exception;
	
	@POST
	@Path("/predictionStat")
	@Produces("application/json")
	public Response predcitionStat(LoginResponse request) throws Exception;
	
	
	@POST
	@Path("/submitpredictionStat")
	@Produces("application/json")
	public Response submitPredcitionStat(PredictionStat request) throws Exception;
	
	
	@POST
	@Path("/trainingGroups")
	@Produces("application/json")
	public Response trainingGroups(TrainingGroupRequest request) throws Exception;
	
	@POST
	@Path("/similartrainingGroups")
	@Produces("application/json")
	public Response similarTrainingGroups(TrainingGroup request) throws Exception;
	
	
	@POST
	@Path("/dashselectedclaimdetails")
	@Produces("application/json")
	// List for selectedclaimlist
	public Response getDashSelectedClaimDetails(ClaimRequest request) throws Exception;
	
	
	@POST
	@Path("/newassginedclaimlist")
	@Produces("application/json")
	public Response getnewmyAssignedClaims(UserModel request) throws Exception;
	
	@POST
	@Path("/myprocessedclaims")
	@Produces("application/json")
	public Response myProcessedClaims(LoginResponse request) throws Exception;
	
	
	
}