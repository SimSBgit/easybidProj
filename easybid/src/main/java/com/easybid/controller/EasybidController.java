package com.easybid.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.easybid.service.EasybidService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/easybid")
public class EasybidController {
	
	private final EasybidService easybidService;
	@GetMapping
	public String getMain(Model model) {
		model.addAttribute("items", easybidService.getAll());
		return "easybidMain";
	}
	
}
