<%@ page import="fr.cg44.plugin.syndicats.util.SyndicatsUtil" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ include file='/jcore/doInitPage.jspf' %>
<% jcmsContext.addCSSHeader("plugins/SyndicatsPlugin/css/plugin.css"); %>
<% jcmsContext.addCSSHeader("plugins/SyndicatsPlugin/css/syndicats.css"); %>
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
	<form class="facets" name="abonnement" action="plugins/SyndicatsPlugin/jsp/abonnement.jsp" method="POST">
		<jalios:select>
	  		<jalios:if predicate="<%=isAbonne %>">
	  			<div>Vous êtes actuellement abonné·e. Si vous souhaitez vous désabonner et ne plus recevoir de courriels vous avertissant des mises à jour, cliquez sur le lien ci-dessous :</div>
	  			<input type="submit" value="Je me désabonne de l'espace syndical" id="opDesabo" name="opDesabo" class="ajax-refresh bouton-abo-newsletter">
	  		</jalios:if>
	  		<jalios:default>
	  			<div>Vous n'êtes pas actuellement abonné·e. Si vous souhaitez vous abonner afin de recevoir un courriel lors d'une mise à jour sur cet espace, cliquez sur le lien ci-dessous :</div>
	  			<input type="submit" value="Je m'abonne à l'espace syndical" id="opAbo" name="opAbo" class="ajax-refresh bouton-abo-newslettere">
	  		</jalios:default>
	  	</jalios:select>
	</form>
</div>

