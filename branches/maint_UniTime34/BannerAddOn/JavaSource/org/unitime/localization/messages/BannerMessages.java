package org.unitime.localization.messages;

import org.unitime.banner.action.BannerCourseSearchAction;
import org.unitime.localization.messages.CourseMessages;

import com.google.gwt.i18n.client.Messages.DefaultMessage;

public interface BannerMessages extends CourseMessages {

	@DefaultMessage("Search")
	@StrutsAction(
		value = "searchBannerCourses",
		apply = BannerCourseSearchAction.class
	)
	String actionSearchBannerOfferings();

	@DefaultMessage("Banner Offering data was not correct:  {0}")
	String missingBannerCourseOfferingId(String bannerCourseOfferingId);

	
}
