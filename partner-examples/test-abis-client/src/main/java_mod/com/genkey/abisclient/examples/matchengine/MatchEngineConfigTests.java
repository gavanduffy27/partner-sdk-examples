package com.genkey.abisclient.examples.matchengine;

import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.abisclient.matchengine.LocalMatchEngine;
import com.genkey.abisclient.matchengine.MatchEngineConfiguration;
import com.genkey.abisclient.matchengine.MatchEngineEnums.GKMatcherSwitchParameter;
import com.genkey.abisclient.matchengine.MatchEngineEnums.GKMatcherThresholdParameter;

public class MatchEngineConfigTests extends MatchEngineExample {

	@Override
	protected void runAllExamples() {
		testConfig();
		testConfigSwitchThresholds();
		testConfigDefaults();
	}
	
    /// <summary>
    /// Tests read/write of MatchEngine configuration.
    /// </summary>
    public  void testConfig()
    {
        MatchEngineConfiguration config = LocalMatchEngine.getConfiguration();
        MatchEngineConfiguration current = config;
        showConfiguration(config);
        printResult("Config", config);
    	config.setUseCompactReferences(true);
    	config.setUsePrefilter(false);
        LocalMatchEngine.setConfiguration(config);
        showConfiguration();

        LocalMatchEngine.setConfiguration(current);
        showConfiguration();

        LocalMatchEngine.setThresholdParameter(GKMatcherThresholdParameter.GKThresholdFarMultipler, 12);
        showConfiguration();

        LocalMatchEngine.setSwitchParameter(GKMatcherSwitchParameter.GKSwitchUseLog10ScoreScheme, true);
        showConfiguration();

        LocalMatchEngine.setSwitchParameter(GKMatcherSwitchParameter.GKSwitchUseLegacyScoreScheme, true);
        showConfiguration();

        LocalMatchEngine.setSwitchParameter(GKMatcherSwitchParameter.GKSwitchUseLegacyScoreScheme, false);
        showConfiguration();

        LocalMatchEngine.setSwitchParameter(GKMatcherSwitchParameter.GKSwitchUseLog10ScoreScheme, false);
        showConfiguration();

        LocalMatchEngine.resetConfiguration();
        showConfiguration();


        LocalMatchEngine.setConfiguration(config);
        showConfiguration();

        LocalMatchEngine.resetConfiguration();
        showConfiguration();


    }

    public void testConfigSwitchThresholds()
    {
        // Obtain config the normal way ..
        MatchEngineConfiguration.Settings config = LocalMatchEngine.getConfiguration().getSettings();

    	
    	MatchEngineConfiguration.Settings config2 = LocalMatchEngine.getConfiguration().getSettings();

        //MatchEngineConfiguration config2 = MatchEngineConfiguration.getInstance();
        config2.compactOnImport = LocalMatchEngine.getSwitchParameter(GKMatcherSwitchParameter.GKSwitchCompactOnImport);
        config2.useCompactReferences = LocalMatchEngine.getSwitchParameter(GKMatcherSwitchParameter.GKSwitchUseCompactReferences);
        config2.defaultScoreThreshold = LocalMatchEngine.getThresholdParameter(GKMatcherThresholdParameter.GKThresholdFARScore);
        config2.prefilterEfficiency = LocalMatchEngine.getThresholdParameter(GKMatcherThresholdParameter.GKThresholdPrefilterEfficiency);
        config2.preFilterTriggerSize = (int)LocalMatchEngine.getThresholdParameter(GKMatcherThresholdParameter.GKThresholdPrefilterTriggerSize);
        config2.usePrefilter = LocalMatchEngine.getSwitchParameter(GKMatcherSwitchParameter.GKSwitchUsePrefilter);
        boolean useLegacyScore = LocalMatchEngine.getSwitchParameter(GKMatcherSwitchParameter.GKSwitchUseLegacyScoreScheme);
        boolean useTenLog10Far10Scheme = LocalMatchEngine.getSwitchParameter(GKMatcherSwitchParameter.GKSwitchUseLog10ScoreScheme);

        // view the current configuration
        printHeaderResult("Config1", LocalMatchEngine.getDescriptionConfig());

        // View the configuration we just retrieved
        LocalMatchEngine.getConfiguration().applySettings(config2);
        printHeaderResult("Config2 Update", LocalMatchEngine.getDescriptionConfig());

        // reset back to what it was before
        LocalMatchEngine.getConfiguration().applySettings(config);
        printHeaderResult("Config1 (restored)", LocalMatchEngine.getDescriptionConfig());
    }

    public void testConfigDefaults()
    {
        // Save current for later restore
        MatchEngineConfiguration.Settings current = LocalMatchEngine.getConfiguration().getSettings();
        // Show current configuration
        showConfiguration();

		boolean useStandardConfig = LocalMatchEngine.getConfiguration().isUseStandardConfig();
		printResult("useStandardConfig initial", useStandardConfig);

	
        // Set to standard settings
        LocalMatchEngine.setUseStandardConfig(true);
        showConfiguration();

        LocalMatchEngine.setSwitchParameter(GKMatcherSwitchParameter.GKSwitchUseLegacyScoreScheme, true);

	
        MatchEngineConfiguration config = LocalMatchEngine.getConfiguration();

	
        config.setFingerUnknownConstraints( new int [] {2,7,3,8});
        LocalMatchEngine.setConfiguration(config);

	
        showConfiguration();
	

        // Calling reset restores standard config
        LocalMatchEngine.resetConfiguration();
        showConfiguration();


        // Set to legacy defaults
        LocalMatchEngine.setUseStandardConfig(false);
        showConfiguration();
        LocalMatchEngine.setSwitchParameter(GKMatcherSwitchParameter.GKSwitchUseLegacyScoreScheme, false);
        LocalMatchEngine.setSwitchParameter(GKMatcherSwitchParameter.GKSwitchUsePrefilter, true);
        showConfiguration();

        // Calling reset restores legacy config because UseStandardConfig is false
        LocalMatchEngine.resetConfiguration();
        showConfiguration();

	
        //restore to current
        int [] constraints = current.fingerUnknownConstraints;
        printResult("Constraints", constraints);
        LocalMatchEngine.getConfiguration().applySettings(current);
        showConfiguration();

    }
	

}
