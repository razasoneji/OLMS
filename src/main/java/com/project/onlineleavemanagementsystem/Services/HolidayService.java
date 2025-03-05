package com.project.onlineleavemanagementsystem.Services;

import com.project.onlineleavemanagementsystem.Entities.Holiday;
import com.project.onlineleavemanagementsystem.Repositories.HolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HolidayService {

    @Autowired
    private final HolidayRepository holidayRepository;

    public List<Holiday> getAllHolidays() {
        return holidayRepository.findAll();
    }

    public Holiday createHoliday(Holiday holiday) {
        return holidayRepository.save(holiday);
    }

    public boolean deleteHoliday(Long holidayId) {
        if (holidayRepository.existsById(holidayId)) {
            holidayRepository.deleteById(holidayId);
            return true;
        }
        return false;
    }
}
