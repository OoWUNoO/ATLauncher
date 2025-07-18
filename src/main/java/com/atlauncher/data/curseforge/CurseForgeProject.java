/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.data.curseforge;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import com.atlauncher.annot.ExcludeFromGsonSerialization;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.json.ModType;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.google.gson.annotations.SerializedName;

public class CurseForgeProject {
    public int id;
    public String name;
    public List<CurseForgeAuthor> authors;
    public int gameId;
    public String summary;
    @ExcludeFromGsonSerialization
    public int downloadCount;
    @ExcludeFromGsonSerialization
    public List<CurseForgeFile> latestFiles;
    public List<CurseForgeCategory> categories;
    public int status;
    public int primaryCategoryId;
    public int classId;
    public String slug;
    public boolean isFeatured;
    public String dateModified;
    public String dateCreated;
    public String dateReleased;
    public Map<String, String> links = new HashMap<>();
    public @Nullable List<CurseForgeSocialLink> socialLinks;
    public CurseForgeAttachment logo = null;
    public Boolean allowModDistribution;

    @SerializedName(value = "screenshots", alternate = { "attachments" })
    public List<CurseForgeAttachment> screenshots;

    @SerializedName(value = "latestFilesIndexes", alternate = { "gameVersionLatestFiles" })
    @ExcludeFromGsonSerialization
    public List<CurseForgeGameVersionLatestFiles> latestFilesIndexes;

    @SerializedName(value = "mainFileId", alternate = { "defaultFileId" })
    public int mainFileId;

    public ModType getModType() {
        if (getRootCategoryId() == Constants.CURSEFORGE_RESOURCE_PACKS_SECTION_ID) {
            return ModType.resourcepack;
        }

        if (getRootCategoryId() == Constants.CURSEFORGE_SHADER_PACKS_SECTION_ID
            || classId == Constants.CURSEFORGE_SHADER_PACKS_SECTION_ID) {
            return ModType.shaderpack;
        }

        return ModType.mods;
    }

    public int getRootCategoryId() {
        Optional<CurseForgeCategory> primaryCategory = categories.stream().filter(c -> c.id == primaryCategoryId)
            .findFirst();

        return primaryCategory
            .map(curseForgeCategory -> curseForgeCategory.classId)
            .orElse(Constants.CURSEFORGE_MODS_SECTION_ID);
    }

    public Optional<CurseForgeAttachment> getLogo() {
        return Optional.ofNullable(logo);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CurseForgeProject)) {
            return false;
        }

        return id == ((CurseForgeProject) object).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String getWebsiteUrl() {
        return links.get("websiteUrl");
    }

    public boolean hasWebsiteUrl() {
        return links.containsKey("websiteUrl") && links.get("websiteUrl") != null && !links.get("websiteUrl").isEmpty();
    }

    public String getIssuesUrl() {
        return links.get("issuesUrl");
    }

    public boolean hasIssuesUrl() {
        return links.containsKey("issuesUrl") && links.get("issuesUrl") != null && !links.get("issuesUrl").isEmpty();
    }

    public String getWikiUrl() {
        return links.get("wikiUrl");
    }

    public boolean hasWikiUrl() {
        return links.containsKey("wikiUrl") && links.get("wikiUrl") != null && !links.get("wikiUrl").isEmpty();
    }

    public @Nullable String getSocialLink(CurseForgeSocialLinkType type) {
        if (socialLinks == null) {
            return null;
        }

        return socialLinks.stream()
            .filter(link -> link.type == type)
            .map(link -> link.url)
            .findFirst()
            .orElse(null);
    }

    public boolean hasSocialLink(CurseForgeSocialLinkType type) {
        if (socialLinks == null) {
            return false;
        }

        return socialLinks.stream()
            .anyMatch(link -> link.type == type && link.url != null
                && !link.url.isEmpty());
    }

    public String getClassUrlSlug() {
        if (classId == Constants.CURSEFORGE_RESOURCE_PACKS_SECTION_ID) {
            return "texture-packs";
        } else if (classId == Constants.CURSEFORGE_MODPACKS_SECTION_ID) {
            return "modpacks";
        }

        return "mc-mods";
    }

    public String getBrowserDownloadUrl(CurseForgeFile file) {
        if (hasWebsiteUrl()) {
            return String.format(Locale.ENGLISH, "%s/download/%d", getWebsiteUrl(), file.id);
        }

        return String.format(Locale.ENGLISH, "https://www.curseforge.com/minecraft/%s/%s/download/%d",
            getClassUrlSlug(), slug,
            file.id);
    }

    public Path getInstanceDirectoryPath(Path root) {
        if (getRootCategoryId() == Constants.CURSEFORGE_RESOURCE_PACKS_SECTION_ID) {
            return root.resolve("resourcepacks");
        }

        if (getRootCategoryId() == Constants.CURSEFORGE_WORLDS_SECTION_ID) {
            return root.resolve("saves");
        }

        if (getRootCategoryId() == Constants.CURSEFORGE_SHADER_PACKS_SECTION_ID
            || classId == Constants.CURSEFORGE_SHADER_PACKS_SECTION_ID) {
            return root.resolve("shaderpacks");
        }

        if (getRootCategoryId() == Constants.CURSEFORGE_PLUGINS_SECTION_ID) {
            return root.resolve("plugins");
        }

        return root.resolve("mods");
    }

    public AnalyticsEvent getAnalyticsEventForAdded(CurseForgeFile file) {
        if (getRootCategoryId() == Constants.CURSEFORGE_RESOURCE_PACKS_SECTION_ID) {
            return AnalyticsEvent.forAddedResourcePack(this, file);
        }

        if (getRootCategoryId() == Constants.CURSEFORGE_WORLDS_SECTION_ID) {
            return AnalyticsEvent.forAddedWorld(this, file);
        }

        if (getRootCategoryId() == Constants.CURSEFORGE_SHADER_PACKS_SECTION_ID
            || classId == Constants.CURSEFORGE_SHADER_PACKS_SECTION_ID) {
            return AnalyticsEvent.forAddedShaders(this, file);
        }

        if (getRootCategoryId() == Constants.CURSEFORGE_PLUGINS_SECTION_ID) {
            return AnalyticsEvent.forAddedPlugin(this, file);
        }

        return AnalyticsEvent.forAddedMod(this, file);
    }
}
