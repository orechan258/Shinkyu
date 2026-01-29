package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.repository.GroupRepository;

@Controller
public class CsvExportController {

	private final GroupRepository groupRepository;

	public CsvExportController(GroupRepository groupRepository) {
		this.groupRepository = groupRepository;
	}

	@GetMapping("/export/csv")
	public String showExportPage(Model model) {
		model.addAttribute("groups", groupRepository.findAll());
		return "admin/export";
	}
}
