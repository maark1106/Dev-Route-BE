package com.teamdevroute.devroute.video;

import static com.teamdevroute.devroute.video.constans.ApiConstans.UDEMY_API_URL_FRONT_VIDEOID;
import static com.teamdevroute.devroute.video.constans.ApiConstans.YOUTUBE_API_URL_FRONT_VIDEOID;
import static com.teamdevroute.devroute.video.enums.PlatformName.Infrean;
import static com.teamdevroute.devroute.video.enums.PlatformName.Udemy;
import static com.teamdevroute.devroute.video.enums.PlatformName.Youtube;

import com.teamdevroute.devroute.crawling.InfreanVideoCrawling;
import com.teamdevroute.devroute.video.dto.infrean.InfreanVideoDTO;
import com.teamdevroute.devroute.video.dto.udemy.UdemyApiResponse;
import com.teamdevroute.devroute.video.dto.udemy.UdemyVideoDTO;
import com.teamdevroute.devroute.video.dto.youtube.YouTubeApiResponse;
import com.teamdevroute.devroute.video.dto.youtube.YoutubeVideoDTO;
import com.teamdevroute.devroute.video.enums.TechnologyStackName;
import com.teamdevroute.devroute.video.fetchers.InfreanVideoFetcher;
import com.teamdevroute.devroute.video.fetchers.UdemyVideoFetcher;
import com.teamdevroute.devroute.video.fetchers.YoutubeVideoFetcher;
import java.io.IOException;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VideoService {

    private final VideoRepository videoRepository;
    private final YoutubeVideoFetcher youtubeVideoFetcher;
    private final UdemyVideoFetcher udemyVideoFetcher;
    private final InfreanVideoFetcher infreanVideoFetcher;

    public VideoService(VideoRepository videoRepository, YoutubeVideoFetcher youtubeVideoFetcher,
                        UdemyVideoFetcher udemyVideoFetcher, InfreanVideoFetcher infreanVideoFetcher) {
        this.videoRepository = videoRepository;
        this.youtubeVideoFetcher = youtubeVideoFetcher;
        this.udemyVideoFetcher = udemyVideoFetcher;
        this.infreanVideoFetcher = infreanVideoFetcher;
    }

    public void fetchAndSaveVideo() throws IOException {
        fetchAndSaveYoutubeVideos();
        fetchAndSaveUdemyVideos();
        fetchAndSaveInfreanVideos();
    }

    public void fetchAndSaveYoutubeVideos() {
        for (TechnologyStackName value : TechnologyStackName.values()) {
            YouTubeApiResponse response = youtubeVideoFetcher.fetchYoutubeVideos(value);
            if (response != null) {
                saveYoutubeVideo(response, value);
            }
        }
    }

    public void fetchAndSaveUdemyVideos() {
        for (TechnologyStackName value : TechnologyStackName.values()) {
            UdemyApiResponse response = udemyVideoFetcher.fetchUdemyVideos(value);
            if (response != null) {
                saveUdemyVideo(response, value);
            }
        }
    }

    public void fetchAndSaveInfreanVideos() throws IOException {
        for (TechnologyStackName value : TechnologyStackName.values()) {
            ArrayList<InfreanVideoDTO> infreanVideoDTOS = infreanVideoFetcher.fetchInfreanVideos(value);
            saveInfreanVideo(infreanVideoDTOS, value);
        }
    }

    private void saveYoutubeVideo(YouTubeApiResponse response, TechnologyStackName techStack) {
        Long rank = 0L;
        for (YouTubeApiResponse.Item item : response.getItems()) {
            if (item.getId() == null) continue;
            String videoId = item.getId().getVideoId();
            String videoUrl = YOUTUBE_API_URL_FRONT_VIDEOID + videoId;
            String title = item.getSnippet().getTitle();
            String thumbnailUrl = item.getSnippet().getThumbnails().getDefault().getUrl();
            if (videoId != null && title != null && thumbnailUrl != null) {
                videoRepository.save(new YoutubeVideoDTO(videoUrl, title, thumbnailUrl).toEntity(
                        String.valueOf(Youtube), String.valueOf(techStack), 0L, ++rank));
            }
        }
    }

    private void saveUdemyVideo(UdemyApiResponse response, TechnologyStackName techStack) {
        Long rank = 0L;
        int currentCourseNumber = 0;
        for (UdemyApiResponse.Course course : response.getResults()) {
            String videoUrl = UDEMY_API_URL_FRONT_VIDEOID + course.getUrl();
            String title = course.getTitle();
            String thumbnailUrl = course.getImage_125_H();
            Long price = Long.valueOf(course.getPrice().replaceAll("[^\\d]", ""));
            if (course.getUrl() != null && title != null && thumbnailUrl != null && price != null) {
                videoRepository.save(new UdemyVideoDTO(videoUrl, title, thumbnailUrl, price).toEntity(
                        String.valueOf(Udemy), String.valueOf(techStack), 0L, ++rank));
                currentCourseNumber += 1;
            }
            if (currentCourseNumber >= 10) {
                break;
            }
        }
    }
    private void saveInfreanVideo(ArrayList<InfreanVideoDTO> infreanVideoDTOS, TechnologyStackName techStack) {
        Long rank = 0L;
        for (InfreanVideoDTO infreanVideoDTO : infreanVideoDTOS) {
            videoRepository.save(infreanVideoDTO.toEntity(String.valueOf(Infrean),
                    String.valueOf(techStack), 0L, ++rank));
        }
    }
}
