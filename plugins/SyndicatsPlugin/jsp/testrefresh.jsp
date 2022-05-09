<%@ page import="fr.cg44.plugin.syndicats.util.SyndicatsUtil" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ include file='/jcore/doInitPage.jspf' %>

<%
/* On regarde si l'utilisateur possède déjà une notification dans cet espace de travail */
boolean isAbonne = SyndicatsUtil.isAbonne(loggedMember, workspace);


// Demande d'abonnement
if(Util.notEmpty(request.getParameter("opAbo"))){
	if(SyndicatsUtil.addAbonnement(loggedMember, workspace)){
		isAbonne=true;
	}

} else {
// Demande de désabonnement
	if(Util.notEmpty(request.getParameter("opDesabo"))){
		if(SyndicatsUtil.deleteAbonnement(loggedMember, workspace)){
			isAbonne=false;
		}
	}
}

%>

<div class="ajax-refresh-div">
<%@ include file='/jcore/doMessageBox.jsp' %>
	<h2>Abonnement</h2>
	<form name="abonnement" action="plugins/SyndicatsPlugin/jsp/testrefresh.jsp" method="POST">
		<jalios:select>
	  		<jalios:if predicate="<%=isAbonne %>">
	  			<div>Vous êtes actuellement abonnés. Si vous souhaitez vous désabonner et ne plus recevoir d'emails vous avertissant des mises à jour, cliquez sur le lien ci-dessous :</div>
	  			<input type="submit" value="Je me désabonne" id="opDesabo" name="opDesabo" class="ajax-refresh">
	  		</jalios:if>
	  		<jalios:default>
	  			<div>Vous n'êtes pas actuellement abonnés. Si vous souhaitez vous abonner afin de recevoir un email lors d'une mise à jour sur cet espace, cliquez sur le lien ci-dessous :</div>
	  			<input type="submit" value="Je m'abonne" id="opAbo" name="opAbo" class="ajax-refresh">
	  		</jalios:default>
	  	</jalios:select>
	</form>
</div>

