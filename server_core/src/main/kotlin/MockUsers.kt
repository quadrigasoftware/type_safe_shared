package com.quadrigasoftware

import kotlinx.serialization.json.*

object MockUserStore {
    val users = listOf(
        // C-Suite
        createMockUser("Amina", "El-Amin", "amina.el-amin@quadrigasoftware.com", null, "CEO", "/fictum", "Exec", "10"),
        createMockUser("Kenji", "Sato", "kenji.sato@quadrigasoftware.com", "amina.el-amin@quadrigasoftware.com", "COO", "/fictum", "Exec", "10"),
        createMockUser("Amara", "Okafor", "amara.okafor@quadrigasoftware.com", "amina.el-amin@quadrigasoftware.com", "CPO", "/fictum", "Exec", "10"),
        createMockUser("Luca", "Moretti", "luca.moretti@quadrigasoftware.com", "amina.el-amin@quadrigasoftware.com", "CTO", "/fictum", "Exec", "10"),

        // Engineering Management
        createMockUser("Sebastien", "Dubois", "sebastien.dubois@quadrigasoftware.com", "luca.moretti@quadrigasoftware.com", "VP Engineering", "/fictum", "Eng", "9"),
        createMockUser("Sarah", "O'Connor", "sarah.o'connor@quadrigasoftware.com", "sebastien.dubois@quadrigasoftware.com", "Director of Engineering", "/fictum", "Eng", "9"),
        createMockUser("Chloe", "Jensen", "chloe.jensen@quadrigasoftware.com", "sarah.o'connor@quadrigasoftware.com", "TPM", "/fictum", "Eng", "9"),
        createMockUser("Jean-Pierre", "Lefebvre", "jean-pierre.lefebvre@quadrigasoftware.com", "sarah.o'connor@quadrigasoftware.com", "Director of QA", "/fictum", "Eng", "9"),

        // Product & Design Management
        createMockUser("Maya", "Patel", "maya.patel@quadrigasoftware.com", "amara.okafor@quadrigasoftware.com", "Director of Product", "/fictum", "Prod", "9"),
        createMockUser("Isabella", "Rossi", "isabella.rossi@quadrigasoftware.com", "amara.okafor@quadrigasoftware.com", "Director of Design", "/fictum", "Design", "9"),

        // Pod: Platform
        createMockUser("David", "Zhang", "david.zhang@quadrigasoftware.com", "sarah.o'connor@quadrigasoftware.com", "Engineering Manager", "/fictum/platform", "Eng", "4"),
        createMockUser("Sarah", "Miller", "sarah.miller@quadrigasoftware.com", "maya.patel@quadrigasoftware.com", "Product Manager", "/fictum/platform", "Prod", "4"),
        createMockUser("Elena", "Schmidt", "elena.schmidt@quadrigasoftware.com", "isabella.rossi@quadrigasoftware.com", "Designer", "/fictum/platform", "Design", "4"),
        createMockUser("Lars", "van der Berg", "lars.vanderberg@quadrigasoftware.com", "jean-pierre.lefebvre@quadrigasoftware.com", "QA Engineer", "/fictum/platform", "QA", "4"),
        createMockUser("Wei", "Chen", "wei.chen@quadrigasoftware.com", "david.zhang@quadrigasoftware.com", "Software Engineer", "/fictum/platform", "Eng", "4"),
        createMockUser("Hiroshi", "Tanaka", "hiroshi.tanaka@quadrigasoftware.com", "david.zhang@quadrigasoftware.com", "Software Engineer", "/fictum/platform", "Eng", "4"),
        createMockUser("Fatima", "Zahra", "fatima.zahra@quadrigasoftware.com", "david.zhang@quadrigasoftware.com", "Software Engineer", "/fictum/platform", "Eng", "4"),

        // Pod: Carthago
        createMockUser("Xing-Hua", "Li", "xing-hua.li@quadrigasoftware.com", "sarah.o'connor@quadrigasoftware.com", "Engineering Manager", "/fictum/carthago", "Eng", "5"),
        createMockUser("Ahmed", "Hassan", "ahmed.hassan@quadrigasoftware.com", "maya.patel@quadrigasoftware.com", "Product Manager", "/fictum/carthago", "Prod", "5"),
        createMockUser("Sofia", "Rodriguez", "sofia.rodriguez@quadrigasoftware.com", "isabella.rossi@quadrigasoftware.com", "Designer", "/fictum/carthago", "Design", "5"),
        createMockUser("Hans", "Müller", "hans.muller@quadrigasoftware.com", "jean-pierre.lefebvre@quadrigasoftware.com", "QA Lead", "/fictum/carthago", "QA", "5"),
        createMockUser("Anika", "Sharma", "anika.sharma@quadrigasoftware.com", "xing-hua.li@quadrigasoftware.com", "Software Engineer", "/fictum/carthago", "Eng", "5"),
        createMockUser("Mateo", "Gomez", "mateo.gomez@quadrigasoftware.com", "xing-hua.li@quadrigasoftware.com", "Software Engineer", "/fictum/carthago", "Eng", "5"),
        createMockUser("Sia", "Nyoni", "sia.nyoni@quadrigasoftware.com", "xing-hua.li@quadrigasoftware.com", "Software Engineer", "/fictum/carthago", "Eng", "5"),

        // Pod: Britannia
        createMockUser("Nadir", "Al-Farsi", "nadir.al-farsi@quadrigasoftware.com", "sarah.o'connor@quadrigasoftware.com", "Engineering Manager", "/fictum/britannia", "Eng", "6"),
        createMockUser("Emma", "Watson", "emma.watson@quadrigasoftware.com", "maya.patel@quadrigasoftware.com", "Product Manager", "/fictum/britannia", "Prod", "6"),
        createMockUser("Olivia", "de Jong", "olivia.dejong@quadrigasoftware.com", "isabella.rossi@quadrigasoftware.com", "Designer", "/fictum/britannia", "Design", "6"),
        createMockUser("Thomas", "Brown", "thomas.brown@quadrigasoftware.com", "jean-pierre.lefebvre@quadrigasoftware.com", "QA Engineer", "/fictum/britannia", "QA", "6"),
        createMockUser("Yuki", "Sato", "yuki.sato@quadrigasoftware.com", "nadir.al-farsi@quadrigasoftware.com", "Software Engineer", "/fictum/britannia", "Eng", "6"),
        createMockUser("Priya", "Das", "priya.das@quadrigasoftware.com", "nadir.al-farsi@quadrigasoftware.com", "Software Engineer", "/fictum/britannia", "Eng", "6"),
        createMockUser("Carlos", "Silva", "carlos.silva@quadrigasoftware.com", "nadir.al-farsi@quadrigasoftware.com", "Software Engineer", "/fictum/britannia", "Eng", "6")
    )

    private fun createMockUser(
        first: String, last: String, email: String, manager: String?, 
        title: String, orgPath: String, dept: String, floor: String
    ): JsonObject {
        return buildJsonObject {
            put("primaryEmail", email)
            putJsonObject("name") {
                put("givenName", first)
                put("familyName", last)
                put("fullName", "$first $last")
            }
            put("orgUnitPath", orgPath)
            put("employeeTitle", title)
            put("department", dept)
            putJsonArray("relations") {
                if (manager != null) {
                    addJsonObject {
                        put("value", manager)
                        put("type", "manager")
                    }
                }
            }
            putJsonObject("locations") {
                put("floor", floor)
            }
        }
    }
}
