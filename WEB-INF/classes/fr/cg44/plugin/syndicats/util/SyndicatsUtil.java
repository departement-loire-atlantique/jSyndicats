package fr.cg44.plugin.syndicats.util;

import org.apache.log4j.Logger;

import com.jalios.jcms.Channel;
import com.jalios.jcms.ControllerStatus;
import com.jalios.jcms.Member;
import com.jalios.jcms.Notification;
import com.jalios.jcms.NotificationCriteria;
import com.jalios.jcms.NotificationManager;
import com.jalios.jcms.context.JcmsMessage;
import com.jalios.jcms.workspace.Workspace;
import com.jalios.util.Util;


/**
 * Classe utilitaire pour l'espace syndical
 */

public class SyndicatsUtil  {

	private static final Logger LOGGER = Logger.getLogger(SyndicatsUtil.class);
	private static final Channel channel = Channel.getChannel();
	private static final String userLang = channel.getCurrentUserLang();
	
	//private static final JcmsContext context = channel.getCurrentJcmsContext();
	
    /**
     * Vérifie si l'utilisateur connecté possède déjà un abonnement (NotificationCriteria) sur un espace de travail donné.
     * Pour cela on regarde s'il existe une Notification, puis une NotificationCriteria qui aurait l'id du Workspace
     * dans sa requête.
     * 
     * @param member : l'utilisateur à tester
     * @param ws : le workspace à tester
     * @return true si un abonnement a été trouvé. False dans le cas contraire.
     */
	public static boolean isAbonne(Member member, Workspace ws){
		
		// Récupération de la Notification
		Notification notification = member.getNotification();
		
		if(Util.notEmpty(notification)){
			// Récupération des NotificationCriteria
			NotificationCriteria[] criterias = notification.getCriteria();
			
			if(Util.notEmpty(criterias)){
				for(NotificationCriteria criteria : criterias){
					// Un criteria a été trouvé. 
					if(criteria.getQuery().indexOf(ws.getId()) != -1){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
	public static boolean addAbonnement(Member member, Workspace ws){
		Notification notification = member.getNotification();

		// Récupération de la Notification existante
		if(Util.notEmpty(notification)){
			
			// Création du NotificationCriteria
			NotificationCriteria criteria = createCriteria(member, ws);
			
			if(criteria!=null){
				// Ajout du NotificationCriteria à la Notification
				return addCriteriaToNotification(notification, criteria, member); 
			}
			
		} else{
			// Si Notification nulle, on la crée
			notification = createNotification(member);
			
			// Création du NotificationCriteria
			if(notification!=null){
				NotificationCriteria criteria = createCriteria(member, ws);
			
				if(criteria!=null){
					// Ajout du NotificationCriteria à la Notification
					return addCriteriaToNotification(notification, criteria, member);
				}
			}
			
		}
		
		return false;
		
	}
	
	
	public static Notification createNotification(Member member){
		String messageErreur = "Erreur lors de la création de l'abonnement.";
		Member opAuthor = channel.getDefaultAdmin();
		
		if(Util.isEmpty(member.getNotification())){
		    Notification notification = new Notification();
		    notification.performCreate(member);
		    Member memberUpdated = (Member)member.getUpdateInstance();
		    memberUpdated.setNotification(notification);
		    
		    // Check update
		    ControllerStatus status = memberUpdated.checkUpdate(opAuthor);
		    if (status.isOK()) {
		    	memberUpdated.performUpdate(opAuthor);
		    	return notification;
		    } else {
		    	String msg = status.getMessage(userLang);
		    	channel.getCurrentJcmsContext().addMsg(new JcmsMessage(JcmsMessage.Level.ERROR, messageErreur));
		    	LOGGER.error("Espace syndical : Impossible de créer la notification pour l'agent "+ member + ": " + msg);
		    }

		}
		return null;
	}
	
	public static NotificationCriteria createCriteria(Member member, Workspace ws){
		String messageErreur = "Erreur lors de la création de l'abonnement.";
		
		// Construction des paramètres du NotificationCriteria
		int mutationType = NotificationManager.MUTATION_MAJOR;
		int periodType = NotificationManager.DAILY;
		String query = "types=com.jalios.jcms.Content&wrkspc="+ws.getId();
		NotificationCriteria criteria = new NotificationCriteria(mutationType, periodType, query, member);
		
		// Check create
		ControllerStatus status = criteria.checkCreate(member);
		if (status.isOK()) {
			criteria.performCreate(member);
			return criteria;
		} else{
			String msg = status.getMessage(userLang);
			channel.getCurrentJcmsContext().addMsg(new JcmsMessage(JcmsMessage.Level.ERROR, messageErreur));
		    LOGGER.error("Espace syndical : erreur lors de la création du NotificationCriteria pour l'agent "+ member + ". Message : "+msg);
		    return null;
		}
		
	}
	
	public static boolean addCriteriaToNotification(Notification notification, NotificationCriteria criteria, Member member){
		String messageErreur = "Erreur lors de la création de l'abonnement.";
		
		// Ajout du NotificationCriteria à la Notification
		Notification notificationUpdated = (Notification) notification.getUpdateInstance();
		notificationUpdated.addCriteria(criteria);
		
		// Check update
		ControllerStatus status = notificationUpdated.checkUpdate(member);
		if (status.isOK()) {
			String message = "Votre abonnement a bien été créé";
			notificationUpdated.performUpdate(member);
			channel.getCurrentJcmsContext().addMsg(channel.getCurrentJcmsContext().getRequest(), new JcmsMessage(JcmsMessage.Level.SUCCESS, message));
			return true;
		} else {
			String msg = status.getMessage(userLang);
			channel.getCurrentJcmsContext().addMsg(channel.getCurrentJcmsContext().getRequest(), new JcmsMessage(JcmsMessage.Level.ERROR, messageErreur));
		    LOGGER.error("Espace syndical : erreur lors de la mise à jour de la Notification pour l'agent " + member + " : "+msg);
		}
		
		return false;
	}
	
	
	public static boolean deleteAbonnement(Member member, Workspace ws){
		String message = "Votre abonnement a bien été supprimé";
		
		// Récupération de la Notification
		Notification notification = member.getNotification();
		
		if(Util.notEmpty(notification)){
			// Récupération des NotificationCriteria
			NotificationCriteria[] criterias = notification.getCriteria();
			
			// parcours des NotificationCriteria
			boolean suppressionOK=false;
			for(NotificationCriteria criteria : criterias){
				// Un criteria a été trouvé. 
				if(criteria.getQuery().indexOf(ws.getId()) != -1){
					// Suppression du NotificationCriteria de la Notification
					if(removeCriteriaFromNotification(notification, criteria, member)){
						// Suppression du NotificationCriteria
						if(deleteCriteria(criteria, member)){
							suppressionOK=true;		
						}
					}
				}
			}
			if(suppressionOK){
				channel.getCurrentJcmsContext().addMsg(channel.getCurrentJcmsContext().getRequest(), new JcmsMessage(JcmsMessage.Level.SUCCESS, message));
				return true;
			}
			
		}
		return false;
	}
	
	public static boolean removeCriteriaFromNotification(Notification notification, NotificationCriteria criteria, Member member){
		
		Notification notificationUpdated = (Notification) notification.getUpdateInstance();
		notificationUpdated.removeCriteria(criteria);
		
		// Check update
		ControllerStatus status = notificationUpdated.checkUpdate(member);
		if (status.isOK()) {
			//String message = "Votre abonnement a bien été créé";
			notificationUpdated.performUpdate(member);
			//channel.getCurrentJcmsContext().addMsg(new JcmsMessage(JcmsMessage.Level.SUCCESS, message));
			return true;
		} else {
			String msg = status.getMessage(userLang);
			//channel.getCurrentJcmsContext().addMsg(new JcmsMessage(JcmsMessage.Level.ERROR, messageErreur));
		    LOGGER.error("Espace syndical : erreur lors de la mise à jour de la Notification (supression du NotificationCriteria) pour l'agent " + member + " : "+msg);
		}
		
		return false;
	}
	
	public static boolean deleteCriteria(NotificationCriteria criteria, Member member){
		NotificationCriteria criteriaUpdated = (NotificationCriteria) criteria.getUpdateInstance();
		
		// Check delete
		ControllerStatus status = criteriaUpdated.checkDelete(member);
		if (status.isOK()) {
			// String message = "Votre abonnement a bien été supprimé";
			criteriaUpdated.performDelete(member);
			//channel.getCurrentJcmsContext().addMsg(new JcmsMessage(JcmsMessage.Level.SUCCESS, message));
			return true;
		} else {
			String msg = status.getMessage(userLang);
			//channel.getCurrentJcmsContext().addMsg(new JcmsMessage(JcmsMessage.Level.ERROR, messageErreur));
		    LOGGER.error("Espace syndical : erreur lors de la suppression du NotificationCriteria pour l'agent " + member + " : "+msg);
		}
		
		return false;
	}
	
	
}
