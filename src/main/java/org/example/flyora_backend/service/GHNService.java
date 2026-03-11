package org.example.flyora_backend.service;

import org.example.flyora_backend.DTOs.CalculateFeeRequestDTO;
import org.example.flyora_backend.DTOs.CreateOrderRequestDTO;
import org.example.flyora_backend.DTOs.DistrictDTO;
import org.example.flyora_backend.DTOs.ProvinceDTO;
import org.example.flyora_backend.DTOs.TrackOrderRequestDTO;
import org.example.flyora_backend.DTOs.WardDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@Service
public class GHNService {

        @Value("${ghn.token}")
        private String ghnToken;

        @Value("${ghn.shop_id}")
        private String ghnShopId;

        @Value("${ghn.shop.district_id}")
        private int shopDistrictId;

        @Value("${ghn.shop.ward_code}")
        private String shopWardCode;

        private final String API_BASE_URL = "https://online-gateway.ghn.vn/shiip/public-api";

        private final String PROVINCE_API = API_BASE_URL + "/master-data/province";

        private final String DISTRICT_API = API_BASE_URL + "/master-data/district";

        private final String WARD_API = API_BASE_URL + "/master-data/ward";

        private final String FEE_API = API_BASE_URL + "/v2/shipping-order/fee";

        private final String CREATE_ORDER_API = API_BASE_URL + "/v2/shipping-order/create";

        private final String TRACKING_API = API_BASE_URL + "/v2/shipping-order/detail";

        private final RestTemplate restTemplate = new RestTemplate();
        private final ObjectMapper objectMapper = new ObjectMapper();

        private static final String GHN_DETAIL_URL =
            "https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/detail";
        /**
         * Lấy danh sách Tỉnh/Thành từ GHN
         */
        public List<ProvinceDTO> getProvinces() {
                // 1. Tạo headers cho request
                HttpHeaders headers = new HttpHeaders();
                headers.set("Token", ghnToken); // Thêm token xác thực
                headers.setContentType(MediaType.APPLICATION_JSON);

                // 2. Tạo HttpEntity chứa headers (không cần body cho GET request)
                HttpEntity<String> entity = new HttpEntity<>(headers);

                // 3. Gọi API của GHN bằng RestTemplate
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                                PROVINCE_API,
                                HttpMethod.GET,
                                entity,
                                new ParameterizedTypeReference<>() {
                                }); // Kiểu dữ liệu trả về là một Map

                // 4. Lấy phần "data" từ response body của GHN
                List<?> rawData = (List<?>) response.getBody().get("data");

                // 5. Dùng ObjectMapper để chuyển đổi danh sách thô sang danh sách ProvinceDTO
                return objectMapper.convertValue(rawData,
                                new com.fasterxml.jackson.core.type.TypeReference<List<ProvinceDTO>>() {
                                });
        }

        public List<DistrictDTO> getDistricts(int provinceId) {
                // 1. Tạo headers cho request, cần cả Token và ShopId
                HttpHeaders headers = createHeaders();

                // 2. Tạo body cho request chứa province_id
                String requestBody = "{\"province_id\":" + provinceId + "}";

                // 3. Tạo HttpEntity chứa body và headers
                HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

                // 4. Gọi API của GHN bằng phương thức POST
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                                DISTRICT_API,
                                HttpMethod.POST, // GHN yêu cầu dùng POST
                                entity,
                                new ParameterizedTypeReference<>() {
                                });

                // 5. Lấy và chuyển đổi dữ liệu trả về
                List<?> rawData = (List<?>) response.getBody().get("data");
                return objectMapper.convertValue(rawData,
                                new com.fasterxml.jackson.core.type.TypeReference<List<DistrictDTO>>() {
                                });
        }

        private HttpHeaders createHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Token", ghnToken);
                headers.set("ShopId", ghnShopId); // Một số API yêu cầu ShopId
                headers.setContentType(MediaType.APPLICATION_JSON);
                return headers;
        }

        public List<WardDTO> getWard(int districtId) {
                HttpHeaders headers = createHeaders();

                String requestBody = "{\"district_id\":" + districtId + "}";

                HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                                WARD_API,
                                HttpMethod.POST,
                                entity,
                                new ParameterizedTypeReference<>() {
                                });

                // 5. Lấy và chuyển đổi dữ liệu trả về
                List<?> rawData = (List<?>) response.getBody().get("data");
                return objectMapper.convertValue(rawData,
                                new com.fasterxml.jackson.core.type.TypeReference<List<WardDTO>>() {
                                });
        }

        @SuppressWarnings("unchecked")
        public Map<String, Object> calculateFee(CalculateFeeRequestDTO feeRequest) {
                HttpHeaders headers = createHeaders();

                Map<String, Object> requestBody = new HashMap<>();

                // ======================================================================
                // PHẦN BỊ THIẾU CẦN BỔ SUNG ĐẦY ĐỦ VÀO ĐÂY
                // ======================================================================

                // 1. Thêm thông tin người gửi (lấy từ cấu hình server)
                requestBody.put("from_district_id", this.shopDistrictId);
                requestBody.put("from_ward_code", this.shopWardCode);

                // 2. Thêm thông tin người nhận (lấy từ DTO của client)
                requestBody.put("to_district_id", feeRequest.getTo_district_id());
                requestBody.put("to_ward_code", feeRequest.getTo_ward_code());

                // 3. Thêm thông tin gói hàng và dịch vụ (lấy từ DTO của client)
                requestBody.put("weight", feeRequest.getWeight()); // <-- TRƯỜNG GÂY LỖI TRƯỚC ĐÓ
                requestBody.put("length", feeRequest.getLength());
                requestBody.put("width", feeRequest.getWidth());
                requestBody.put("height", feeRequest.getHeight());
                requestBody.put("insurance_value", feeRequest.getInsurance_value());
                Integer serviceId = getServiceId(feeRequest.getTo_district_id());
                requestBody.put("service_id", serviceId);

                // ======================================================================
                // KẾT THÚC PHẦN BỔ SUNG
                // ======================================================================

                // Dòng này để debug, bạn có thể xóa sau khi đã chạy thành công
                System.out.println("Request Body to GHN: " + requestBody.toString());

                // Tạo HttpEntity để đóng gói body và headers
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

                // Gọi API của GHN
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                                FEE_API,
                                HttpMethod.POST,
                                entity,
                                new ParameterizedTypeReference<>() {
                                });

                return (Map<String, Object>) response.getBody().get("data");
        }

        @SuppressWarnings("unchecked")
        public Map<String, Object> createOrder(CreateOrderRequestDTO orderRequest) {
                HttpHeaders headers = createHeaders();

                // DTO đã đầy đủ thông tin, có thể gửi trực tiếp
                HttpEntity<CreateOrderRequestDTO> entity = new HttpEntity<>(orderRequest, headers);

                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                                CREATE_ORDER_API,
                                HttpMethod.POST,
                                entity,
                                new ParameterizedTypeReference<>() {
                                });

                return (Map<String, Object>) response.getBody().get("data");
        }

        @SuppressWarnings("unchecked")
        public Map<String, Object> getOrderStatus(String orderCode) {
                try {
                        HttpHeaders headers = createHeaders();

                        // Tạo request body với order_code
                        TrackOrderRequestDTO request = new TrackOrderRequestDTO();
                        request.setOrder_code(orderCode);

                        // Đóng gói request body và headers
                        HttpEntity<TrackOrderRequestDTO> entity = new HttpEntity<>(request, headers);

                        // Gọi API của GHN, lưu ý phương thức là POST
                        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                                        TRACKING_API,
                                        HttpMethod.POST,
                                        entity,
                                        new ParameterizedTypeReference<>() {
                                        });

                        // Trả về phần 'data' của response từ GHN
                        return (Map<String, Object>) response.getBody().get("data");

                } catch (HttpClientErrorException e) {
                        // Xử lý các trường hợp lỗi như không tìm thấy đơn hàng
                        throw new RuntimeException("Không thể lấy thông tin đơn hàng từ GHN. Chi tiết: "
                                        + e.getResponseBodyAsString(), e);
                }
        }

        public Map<String, Object> getOrderDetail(String trackingNumber) {

        HttpHeaders headers = createHeaders();

        Map<String, String> requestBody = Map.of(
                "order_code", trackingNumber
        );

        HttpEntity<Map<String, String>> entity =
                new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map<String, Object>> response =
                restTemplate.exchange(
                        GHN_DETAIL_URL,
                        HttpMethod.POST,
                        entity,
                        new ParameterizedTypeReference<Map<String, Object>>() {}
                );

        if (response.getStatusCode().is2xxSuccessful()
                && response.getBody() != null) {

                Object dataObj = response.getBody().get("data");

                if (dataObj instanceof Map<?, ?> dataMap) {

                // Convert sang Map<String, Object> an toàn
                return objectMapper.convertValue(
                        dataMap,
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
                );
                }
        }

        throw new RuntimeException("Không lấy được trạng thái từ GHN");
        }

        public Integer getServiceId(int toDistrictId) {

        HttpHeaders headers = createHeaders();

        Map<String, Object> body = new HashMap<>();
        body.put("shop_id", Integer.parseInt(ghnShopId));
        body.put("from_district", shopDistrictId);
        body.put("to_district", toDistrictId);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map<String, Object>> response =
                restTemplate.exchange(
                        API_BASE_URL + "/v2/shipping-order/available-services",
                        HttpMethod.POST,
                        entity,
                        new ParameterizedTypeReference<>() {}
                );

        List<Map<String,Object>> services =
                (List<Map<String,Object>>) response.getBody().get("data");

        return (Integer) services.get(0).get("service_id");
        }
}
