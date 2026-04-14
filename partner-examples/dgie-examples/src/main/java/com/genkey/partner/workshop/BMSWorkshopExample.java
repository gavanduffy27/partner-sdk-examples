package com.genkey.partner.workshop;

import com.genkey.abisclient.ImageBlob;
import com.genkey.abisclient.examples.utils.ExampleTestUtils;
import com.genkey.abisclient.service.ABISServiceModule;
import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.partner.biographic.BiographicService;
import com.genkey.partner.dgie.DGIEServiceModule;
import com.genkey.partner.dgie.LegacyMatchingService;
import com.genkey.partner.example.PartnerExample;

public abstract class BMSWorkshopExample extends PartnerExample {

	GenkeyABISService abisService;
	BiographicService biographicService;
	LegacyMatchingService legacyService;

	@Override
	protected void setUp() {
		ExampleTestUtils.setCodeDefaultSettings();
		ExampleTestUtils.loadDefaultSettings();

		String hostName = PartnerExample.getPrimaryHost();
		int port = PartnerExample.getServicePort();
		String DomainName = PartnerExample.getPartnerDomainName();

		// Initialise core services ..
		// Currently for production this would be //10.22.74.51, 8091, BMS
		DGIEServiceModule.initCoreServices(hostName, port, DomainName);

		// To use Legacy services .. not required
		DGIEServiceModule.initLegacyService(hostName, port);

		abisService = ABISServiceModule.getABISService();
		biographicService = DGIEServiceModule.getBiographicService();
		legacyService = DGIEServiceModule.getLegacyService();

	}

	public GenkeyABISService getAbisService() {
		return abisService;
	}

	public BiographicService getBiographicService() {
		return biographicService;
	}

	public LegacyMatchingService getLegacyService() {
		return legacyService;
	}

	public boolean adjudicateCheck(ImageBlob querySubjectPortrait, ImageBlob matchFace) {
		// TODO Auto-generated method stub
		return false;
	}

}
