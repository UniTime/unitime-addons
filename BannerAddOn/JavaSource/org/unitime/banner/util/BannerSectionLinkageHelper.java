/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/

package org.unitime.banner.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.LazyInitializationException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.banner.model.BannerConfig;
import org.unitime.banner.model.BannerSection;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;


/**
 * @author says
 *
 */
public class BannerSectionLinkageHelper {

	private InstructionalOffering instructionalOffering;
	private Session hibSession;
	private HashMap<SchedulingSubpart, SchedulingSubpart> childToTrueParentSubpart;
	private HashMap<SchedulingSubpart, TreeSet<SchedulingSubpart>> parentToTrueChildSubparts;
	private HashMap<SchedulingSubpart, Boolean> schedulingSubpartHasSiblings; 
	private HashMap<SchedulingSubpart, Boolean> schedulingSubpartIsLastSibling; 
	private HashMap<BannerSection, SchedulingSubpart> bannerSectionToSubpartMap;
	/**
	 * 
	 */
	public BannerSectionLinkageHelper(InstructionalOffering instructionalOffering, Session hibSession) {
		this.instructionalOffering = instructionalOffering;
		this.hibSession = hibSession;
		childToTrueParentSubpart = new HashMap<SchedulingSubpart, SchedulingSubpart>();
		parentToTrueChildSubparts = new HashMap<SchedulingSubpart, TreeSet<SchedulingSubpart>>();
		schedulingSubpartHasSiblings = new HashMap<SchedulingSubpart, Boolean>();
		bannerSectionToSubpartMap = new HashMap<BannerSection, SchedulingSubpart>();
		schedulingSubpartIsLastSibling = new HashMap<SchedulingSubpart, Boolean>();
	}

	public void updateLinkages(){
		Debug.info("Manage linking for an offering");
		for (InstrOfferingConfig ioc : instructionalOffering.getInstrOfferingConfigs()){
			if (ioc.getSchedulingSubparts().isEmpty()){
				continue;
			} else if (ioc.getSchedulingSubparts().size() == 1){
				removeAnyLinksForSingleItypeConfig(ioc);
			} else {
				updateLinksForPartiallyNested(ioc);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void updateLinksForPartiallyNested(InstrOfferingConfig ioc) {
		TreeSet<SchedulingSubpart> topLevel = new TreeSet<SchedulingSubpart>(new SchedulingSubpartComparator());
		for (SchedulingSubpart ss : ioc.getSchedulingSubparts()){
			if (ss.getParentSubpart() == null){
				topLevel.add(ss);
			}
		}

		if (topLevel.size() == 1 && getTrueChildSubparts(topLevel.first()).isEmpty()) {
			removeAnyLinksForSingleItypeConfig(ioc);
		} else {
			HashMap<BannerConfig, HashSet<String>> usedLinkIds = new HashMap<BannerConfig, HashSet<String>>();
			HashMap<BannerSection, HashMap<SchedulingSubpart, String>> idToUseForParentAndSupart = new HashMap<BannerSection, HashMap<SchedulingSubpart,String>>();
			List<BannerConfig> bannerConfigs = BannerConfig.findBannerConfigsForInstrOfferingConfig(ioc, hibSession);
			for (BannerConfig bc : bannerConfigs){
				updateLinksForSubparts(bc, topLevel, usedLinkIds, idToUseForParentAndSupart);
				updateLinkConnectorsForSubparts(bc, topLevel);
			}
		}
	}
	
	private void updateLinkConnectorsForSubparts(BannerConfig bc, TreeSet<SchedulingSubpart> subparts) {
		String currentParentId = null;
		String nextParentId = null;
		String returnId = null;
		boolean hasSiblings = subparts.size() > 1;
		for (SchedulingSubpart ss : subparts){
			for (Class_ c : ss.getClasses()){
				BannerSection bs = BannerSection.findBannerSectionForBannerCourseAndClass(bc.getBannerCourse(), c);
				returnId = updateLinkConnectorForChildrenOf(bs, getTrueChildSubparts(ss));
				if (hasSiblings){
					if (currentParentId != null){
						updateLinkConnectorForBannerSectionIfNecessaryAndSave(bs, currentParentId);
					}
					nextParentId = returnId;
				} else {
					updateLinkConnectorForBannerSectionIfNecessaryAndSave(bs, returnId);
				}
			}

			if (nextParentId != null){
				currentParentId = nextParentId;
			}
		}
		if (hasSiblings){
			SchedulingSubpart ss = subparts.first();
			for (Class_ c : (Set<Class_>) ss.getClasses()){
				BannerSection bs = BannerSection.findBannerSectionForBannerCourseAndClass(bc.getBannerCourse(), c);
				updateLinkConnectorForBannerSectionIfNecessaryAndSave(bs, currentParentId);
			}
		}
	}
	
	
	private HashMap<Integer, HashSet<BannerSection>> groupChildSectionsByItype(BannerSection bs){
		HashMap<Integer, HashSet<BannerSection>> sections = new HashMap<Integer, HashSet<BannerSection>>();
		Integer itypeId = null;
		HashSet<BannerSection> aSet = null;
		if (bs != null && bs.getBannerSectionToChildSections() != null){
			for(BannerSection childSection : bs.getBannerSectionToChildSections()){
				if (!childSection.getClasses(hibSession).isEmpty()){
					Class_ c = (Class_)childSection.getClasses(hibSession).iterator().next();
					itypeId = c.getSchedulingSubpart().getItype().getItype();
					aSet = sections.get(itypeId);
					if (aSet == null){
						aSet = new HashSet<BannerSection>();
						sections.put(itypeId, aSet);
					}
					aSet.add(childSection);
				}
			}
		}
		return(sections);
	}

	private String updateLinkConnectorForChildrenOf(BannerSection parentBannerSection, TreeSet<SchedulingSubpart> subparts) {
		
		String currentParentId = null;
		if (parentBannerSection != null){
			currentParentId = parentBannerSection.getLinkIdentifier();
		}
		String nextParentId = null;
		HashMap<Integer, HashSet<BannerSection>> itypeChildSectionMap = groupChildSectionsByItype(parentBannerSection);
		for(Iterator<SchedulingSubpart> it = subparts.iterator(); it.hasNext();){
			SchedulingSubpart ss = it.next();
			if (ss != null && itypeChildSectionMap != null && !itypeChildSectionMap.isEmpty()){
				for(BannerSection bs : itypeChildSectionMap.get(ss.getItype().getItype())){
					if (bs != null){
						updateLinkConnectorForBannerSectionIfNecessaryAndSave(bs, currentParentId);
						nextParentId = updateLinkConnectorForChildrenOf(bs, getTrueChildSubparts(ss));
					}
				}
				currentParentId = nextParentId;
			}
		}
		if (nextParentId == null && parentBannerSection != null){
			return(parentBannerSection.getLinkIdentifier());
		} else {
			return(nextParentId);
		}
	}

	private BannerSection findTopParentBannerSection(BannerSection bs, SchedulingSubpart ss){
		if (bs.getParentBannerSection() == null){
			return(bs);
		}  else {
			SchedulingSubpart parentSchedulingSubpart = getTrueParentSubpart(ss);
			if (parentSchedulingSubpart != null) {		
				TreeSet<SchedulingSubpart> childSubparts = getTrueChildSubparts(parentSchedulingSubpart);
				if (childSubparts.size() > 1){
					if (hasGrandparentWithSiblings(bs, ss)){
						if (childSubparts.last().getUniqueId().equals(ss.getUniqueId())){
							return(findTopParentBannerSection(bs.getParentBannerSection(), parentSchedulingSubpart));
						} else {
							return(bs.getParentBannerSection());
						}
					} else {
						return(bs.getParentBannerSection());
					}
				} else if (childSubparts.size() == 1){
					return(findTopParentBannerSection(bs.getParentBannerSection(), parentSchedulingSubpart));
				}			
			} else {
				return(bs);
			}
		}
		return(null);
	}
	
	private SchedulingSubpart getTrueParentSubpart(SchedulingSubpart ss){
		if (childToTrueParentSubpart.containsKey(ss)){
			return(childToTrueParentSubpart.get(ss));
		}
		if (ss.getParentSubpart() == null){
			childToTrueParentSubpart.put(ss, null);
			return(null);
		}
		SchedulingSubpart tempSchedulingSubpart = ss;
		while(tempSchedulingSubpart.getItype().getItype().equals(ss.getItype().getItype())&& tempSchedulingSubpart.getParentSubpart() != null){
			tempSchedulingSubpart = tempSchedulingSubpart.getParentSubpart();
		}
		if (tempSchedulingSubpart.getItype().getItype().equals(ss.getItype().getItype())){
			childToTrueParentSubpart.put(ss, null);
			return(null);
		}
		childToTrueParentSubpart.put(ss, tempSchedulingSubpart);
		return(tempSchedulingSubpart);
	}
	
	
	private void updateLinksForSubparts(BannerConfig bc, TreeSet<SchedulingSubpart> subparts, HashMap<BannerConfig, HashSet<String>> usedLinkIds, HashMap<BannerSection, HashMap<SchedulingSubpart, String>> idToUseForParentAndSupart){
		CourseOffering co = bc.getBannerCourse().getCourseOffering(hibSession);
		for(Iterator<SchedulingSubpart> it = subparts.iterator(); it.hasNext();){
			SchedulingSubpart ss = it.next();
			schedulingSubpartHasSiblings.put(ss, Boolean.valueOf((subparts.size() > 1)));
			schedulingSubpartIsLastSibling.put(ss, !it.hasNext() && subparts.size() > 1);
			boolean hasChildren = !getTrueChildSubparts(ss).isEmpty();
			boolean hasParent = getTrueParentSubpart(ss) != null;
			BannerSection parentSection = null;
			String currentLinkId = null;
			for (Class_ c : ss.getClasses()){
				BannerSection bs = BannerSection.findBannerSectionForClassAndCourseOffering(c, co, hibSession);
				if (bs == null){
					continue;
				}
				bannerSectionToSubpartMap.put(bs, ss);
				parentSection = findTopParentBannerSection(bs, ss);
				if (parentSection.getUniqueId().equals(bs.getUniqueId())){
					parentSection = null;
				}
				if (!hasChildren){ 
					//has no children
					if (hasParent){
						// has parent and no children
						if (schedulingSubpartHasSiblings.get(ss).booleanValue()){
							// has parent, no children, and siblings
							if (bannerSectionToSubpartMap.get(parentSection) != null 
									&& schedulingSubpartHasSiblings.get(bannerSectionToSubpartMap.get(parentSection)) != null 
									&& schedulingSubpartHasSiblings.get(bannerSectionToSubpartMap.get(parentSection)).booleanValue()){
								// has parent with siblings, no children, and siblings
								if (schedulingSubpartIsLastSibling.get(ss).booleanValue()){
									// has parent with siblings, no children, and siblings and is last sibling so
									//     link id should be same across itype
									if (currentLinkId == null){
										currentLinkId = findCurrentUnusedLinkIdentifier(bs, bc, usedLinkIds);
									}
									updateLinkIdForBannerSectionIfNecessaryAndSave(bs, currentLinkId);
								} else {
									// has parent with siblings, no children, and siblings and is not the last sibling so 
									//     link id should be same across parent section
									currentLinkId = getCurrentLinkId(currentLinkId, bs, bc, ss, parentSection, usedLinkIds, idToUseForParentAndSupart);
									updateLinkIdForBannerSectionIfNecessaryAndSave(bs, currentLinkId);
								}
							} else {
								currentLinkId = getCurrentLinkId(currentLinkId, bs, bc, ss, parentSection, usedLinkIds, idToUseForParentAndSupart);
								updateLinkIdForBannerSectionIfNecessaryAndSave(bs, currentLinkId);								
							}
						} else {
							// has parent and has no children and no siblings
							if (bannerSectionToSubpartMap.get(parentSection) != null 
									&& schedulingSubpartHasSiblings.get(bannerSectionToSubpartMap.get(parentSection)) != null 
									&& schedulingSubpartHasSiblings.get(bannerSectionToSubpartMap.get(parentSection)).booleanValue()){
								// has parent with siblings and has no children and no siblings so link id should be same across iytpe
								if (schedulingSubpartIsLastSibling.get(bannerSectionToSubpartMap.get(parentSection)).booleanValue()){
									if (parentSection.getParentBannerSection() == null){
										if (currentLinkId == null){
											currentLinkId = findCurrentUnusedLinkIdentifier(bs, bc, usedLinkIds);
										}
									} else {
										currentLinkId = getCurrentLinkId(currentLinkId, bs, bc, ss, parentSection, usedLinkIds, idToUseForParentAndSupart);
									}
								} else {
									if (currentLinkId == null){
										currentLinkId = findCurrentUnusedLinkIdentifier(bs, bc, usedLinkIds);
									}
								}
								updateLinkIdForBannerSectionIfNecessaryAndSave(bs, currentLinkId);
							} else {
								// has parent with no siblings and has no children and no siblings so link id should be same across parent section
								currentLinkId = getCurrentLinkId(currentLinkId, bs, bc, ss, parentSection, usedLinkIds, idToUseForParentAndSupart);
								updateLinkIdForBannerSectionIfNecessaryAndSave(bs, currentLinkId);

							}
						}
					} else {
						// no children and no parent
						if (schedulingSubpartHasSiblings.get(ss).booleanValue()){
							// no parent and no children and has siblings so link id should be same across itype
							if (currentLinkId == null){
								currentLinkId = findCurrentUnusedLinkIdentifier(bs, bc, usedLinkIds);
							}							
							updateLinkIdForBannerSectionIfNecessaryAndSave(bs, currentLinkId);
						} else {
							// no parent, no children, no siblings so should not have a link id
							updateLinkIdForBannerSectionIfNecessaryAndSave(bs, null);
						}
					}
					
				} else {
					// has children
					currentLinkId = findCurrentUnusedLinkIdentifier(bs, bc, usedLinkIds);
					updateLinkIdForBannerSectionIfNecessaryAndSave(bs, currentLinkId);
				}
			}
			updateLinksForSubparts(bc, getTrueChildSubparts(ss), usedLinkIds, idToUseForParentAndSupart);
		}
	}
	
	private String getCurrentLinkId(String currentId, BannerSection bs, BannerConfig bc, SchedulingSubpart ss, BannerSection parentSection, HashMap<BannerConfig, HashSet<String>> usedLinkIds, HashMap<BannerSection, HashMap<SchedulingSubpart, String>> idToUseForParentAndSupart){
		String currentLinkId = currentId;
		HashMap<SchedulingSubpart, String> idMap = idToUseForParentAndSupart.get(parentSection);
		String aLinkId = null;
		if (idMap == null){
			idMap = new HashMap<SchedulingSubpart, String>();
			idToUseForParentAndSupart.put(parentSection, idMap);
		}
		if (idMap.containsKey(ss)){
			aLinkId = idMap.get(ss);
		}
		if (currentLinkId == null && aLinkId != null){
			currentLinkId = aLinkId;
		} else if (currentLinkId == null && aLinkId == null){
			currentLinkId = findCurrentUnusedLinkIdentifier(bs, bc, usedLinkIds);
			idMap.put(ss, currentLinkId);
		} else if (currentLinkId != null && aLinkId == null){
			currentLinkId = findCurrentUnusedLinkIdentifier(bs, bc, usedLinkIds);
			idMap.put(ss, currentLinkId);
		} else if (!currentLinkId.equals(aLinkId)){
			currentLinkId = aLinkId;
		}
		return(currentLinkId);		
	}

	private String findCurrentUnusedLinkIdentifier(BannerSection bs, BannerConfig bc, HashMap<BannerConfig, HashSet<String>> usedLinkIds){
		String currentLinkId = bs.getLinkIdentifier();
		if (currentLinkId == null 
				|| (usedLinkIds.get(bc) != null && usedLinkIds.get(bc).contains(currentLinkId))){
			currentLinkId = BannerSection.findNextUnusedLinkIdentifierFor(bs.getSession(), bc.getBannerCourse().getCourseOffering(hibSession), hibSession);
		}
		HashSet<String> hs = usedLinkIds.get(bc);
		if (hs == null){
			hs = new HashSet<String>();
			usedLinkIds.put(bc, hs);
		}
		hs.add(currentLinkId);						
		return(currentLinkId);
	}

	private void updateLinkIdForBannerSectionIfNecessaryAndSave(BannerSection bs, String linkIdentifier){
		if (bs.getLinkIdentifier() == null || !bs.getLinkIdentifier().equals(linkIdentifier)){
			bs.setLinkIdentifier(linkIdentifier);
			saveChangesToBannerSectionIfNecessary(bs);
		}
	}

	private void updateLinkConnectorForBannerSectionIfNecessaryAndSave(BannerSection bs, String linkConnector){
		if (bs == null){
			return;
		}
		if (bs.getLinkConnector() == null || !bs.getLinkConnector().equals(linkConnector)){
			bs.setLinkConnector(linkConnector);
			saveChangesToBannerSectionIfNecessary(bs);
		}
	}

	private void saveChangesToBannerSectionIfNecessary(BannerSection bs){
		Transaction trans = hibSession.getTransaction();
		if (trans == null) {
			trans = hibSession.beginTransaction();
		} else if (!trans.isActive()) {
			trans.begin();
		}
		hibSession.merge(bs);
		trans.commit();
		hibSession.flush();
	}
	
	
	
	private TreeSet<SchedulingSubpart> getTrueChildSubparts(SchedulingSubpart ss){
		if (parentToTrueChildSubparts.containsKey(ss)){
			return(parentToTrueChildSubparts.get(ss));
		}
		TreeSet<SchedulingSubpart> childSubparts = new TreeSet<SchedulingSubpart>(new SchedulingSubpartComparator());
		try {
			if (!ss.getChildSubparts().isEmpty()){
				for (SchedulingSubpart childSubpart : ss.getChildSubparts()){
					if (childSubpart.getItype().getItype().equals(ss.getItype().getItype())){
						childSubparts.addAll(getTrueChildSubparts(childSubpart));
					} else {
						childSubparts.add(childSubpart);
					}
				}
			}
		} catch (LazyInitializationException e) {
			Debug.warning(e.getMessage());
			for (SchedulingSubpart childSubpart: ss.getInstrOfferingConfig().getSchedulingSubparts()) {
				if (ss.equals(childSubpart.getParentSubpart())) {
					if (childSubpart.getItype().getItype().equals(ss.getItype().getItype())){
						childSubparts.addAll(getTrueChildSubparts(childSubpart));
					} else {
						childSubparts.add(childSubpart);
					}
				}
			}
		}
		parentToTrueChildSubparts.put(ss, childSubparts);
		return(childSubparts);
	}
		
	private boolean hasGrandparentWithSiblings(BannerSection bs, SchedulingSubpart ss){
		return(getFirstGrandparentWithSiblings(bs, ss) != null);
	}
	
	private BannerSection getFirstGrandparentWithSiblings(BannerSection bs, SchedulingSubpart ss){
		if (bs.getParentBannerSection() == null){
			return(null);
		}
		if (bs.getParentBannerSection().getParentBannerSection() == null){
			return(null);
		}
		BannerSection grandparentBannerSection = bs.getParentBannerSection().getParentBannerSection();
		SchedulingSubpart grandparentSchedulingSubpart = getTrueParentSubpart(getTrueParentSubpart(ss));
		if (schedulingSubpartHasSiblings.get(grandparentSchedulingSubpart).booleanValue()){
			return(grandparentBannerSection);
		}
		while(grandparentBannerSection.getParentBannerSection() != null){
			grandparentBannerSection = bs.getParentBannerSection().getParentBannerSection();
			grandparentSchedulingSubpart = getTrueParentSubpart(getTrueParentSubpart(ss));
			if (schedulingSubpartHasSiblings.get(grandparentSchedulingSubpart).booleanValue()){
				return(grandparentBannerSection);
			}
		}
		return(null);
	}

	private void removeAnyLinksForSingleItypeConfig(InstrOfferingConfig ioc) {
		SchedulingSubpart ss = (SchedulingSubpart)ioc.getSchedulingSubparts().iterator().next();
		for (Class_ c : ss.getClasses()){
			List<BannerSection> sections = BannerSection.findBannerSectionsForClass(c, hibSession);
			for(BannerSection bs : sections){
				if (bs.getLinkConnector() != null || bs.getLinkIdentifier() != null){
					bs.setLinkConnector(null);
					bs.setLinkIdentifier(null);
					hibSession.merge(bs);
				}
			}
		}
	}
}
