package com.mitrian.diploma.voting.room.filter.controller;

import com.mitrian.diploma.voting.room.filter.dto.KitchenTagDTO;
import com.mitrian.diploma.voting.room.filter.service.RoomFilterService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/kitchen-tags")
public class KitchenTagController {

	private final RoomFilterService roomFilterService;

	public KitchenTagController(RoomFilterService roomFilterService) {
		this.roomFilterService = roomFilterService;
	}

	@GetMapping
	public List<KitchenTagDTO> listCatalog() {
		return roomFilterService.listKitchenTagsCatalog();
	}
}
