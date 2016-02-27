# simple makefile for gradle project

GRADLE := gradle

define GRADLE_TARGET
# param: PROJECT TARGET TASK [default]
ifeq ($(4),default)
$(2): $(1)-$(2)
.PHONY: $(2)
endif

$(1)-$(2):
	$(GRADLE) ":$(1):$(3)"
.PHONY: $(1)-$(2)
endef

define GRADLE_ANDROID
# param: PROJECT [default]
$$(eval $$(call GRADLE_TARGET,$(1),debug,assembleDebug,$(2)))
$$(eval $$(call GRADLE_TARGET,$(1),release,assembleRelease,$(2)))
$$(eval $$(call GRADLE_TARGET,$(1),clean,clean,$(2)))
$$(eval $$(call GRADLE_TARGET,$(1),install,installDebug,$(2)))
$$(eval $$(call GRADLE_TARGET,$(1),uninstall,uninstallDebug,$(2)))
endef

$(eval $(call GRADLE_ANDROID,SystemSettingsEditor,default))
