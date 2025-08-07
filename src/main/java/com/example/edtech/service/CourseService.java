package com.example.edtech.service;

import com.example.edtech.dto.Coursedto;
import com.example.edtech.dto.Lecturedto;
import com.example.edtech.entity.CourseEntity;
import com.example.edtech.entity.LectureEntity;
import com.example.edtech.entity.UserEntity;
import com.example.edtech.repository.CourseRepository;
import com.example.edtech.repository.LectureRepository;
import com.example.edtech.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.security.Security;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

@Service
public class CourseService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    LectureRepository lectureRepository;

    public boolean save(Coursedto dto) {
        CourseEntity course = CourseEntity.builder().title(dto.getTitle()).description(dto.getDescription()).category(dto.getCategory()).thumbnailUrl(dto.getThumbnailUrl()).build();
        try {
            course.setCreatedAt(LocalDateTime.now());
            course.setUpdatedAt(LocalDateTime.now());

            course.setCreatedBy(getCurrentUserId());
            Objects.requireNonNull(courseRepository.save(course));

            return true;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    public ObjectId getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity byEmail = userRepository.findByEmail(authentication.getName());
        return byEmail.getId();
    }

    public boolean isExistsByTitle(String title) {
        return courseRepository.existsByTitle(title);
    }

    public boolean deleteCourse(String id) {
        ObjectId objectId = new ObjectId(id);
        courseRepository.deleteById(objectId);
        return true;
    }

    public boolean isCourseExistsById(ObjectId id) {
        return courseRepository.existsById(id);
    }

    public boolean isValidUserOfCourse(CourseEntity course) throws AccessDeniedException {
        if (!course.getCreatedBy().toHexString().equals(getCurrentUserId().toHexString()))
            throw new AccessDeniedException("You are not allowed to modify this course");
        return true;
    }

    public boolean saveLectureInCourse(Lecturedto lecture,CourseEntity course) {
        String videoUrl=lecture.getVideoUrl();
        if (videoUrl == null || (!videoUrl.startsWith("http://") && !videoUrl.startsWith("https://"))) {
            throw new IllegalArgumentException("Invalid video URL. It must start with http or https.");
        }
        LectureEntity lectureEntity=LectureEntity.builder()
                .title(lecture.getTitle())
                .description(lecture.getDescription())
                .durationInMinutes(lecture.getDurationInMinutes())
                .videoUrl(lecture.getVideoUrl())
                .createdAt(LocalDateTime.now())
                .build();
        LectureEntity savedLecture = lectureRepository.save(lectureEntity);
        if (course.getLectureId() == null) {
            course.setLectureId(new ArrayList<>());
        }
        course.getLectureId().add(savedLecture.getId());
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);
        return true;
    }
}
