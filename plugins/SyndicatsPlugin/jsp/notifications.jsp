<%@ page contentType="text/html; charset=UTF-8" %>
<%@ include file='/jcore/doInitPage.jspf' %>

<%
Notification notification = loggedMember.getNotification();
if(Util.notEmpty(notification)){
	NotificationCriteria[] criterias = notification.getCriteria();
	for(NotificationCriteria criteria : criterias){
		logger.warn(criteria.toFullString());
	}
}

%>