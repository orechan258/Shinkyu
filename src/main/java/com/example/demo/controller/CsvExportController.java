package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.repository.UserGroupRepository;

@Controller
public class CsvExportController {

    private final UserGroupRepository groupRepository;

    public CsvExportController(UserGroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @GetMapping("/export/csv")
    public String showExportPage(Model model) {
        model.addAttribute("groups", groupRepository.findAll());
        return "admin/export";
    }
}
