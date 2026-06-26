@file:Suppress("ktlint:constructor:test-method-naming")

package app.constructor.csdk.problemreport.presentation.impl

import gearsmobile.feature.problem_report.presentation.impl.generated.resources.Res
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.metadata_description
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.metadata_device
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.metadata_os
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.metadata_problem_type
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.metadata_report_id
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.metadata_steps_to_reproduce
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.problem_report_auto_captured
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.problem_report_char_counter_format
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.problem_report_email_subject_format
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.problem_report_output_zip_filename
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.problem_report_submit_error
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.problem_type_account
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.problem_type_bug
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.problem_type_content
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.problem_type_other
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.problem_type_performance
import gearsmobile.feature.problem_report.presentation.impl.generated.resources.problem_type_ui
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.getSystemResourceEnvironment

// ── Italian ───────────────────────────────────────────────────────────────────

class ItalianStringsTest {

    private lateinit var previousLocale: Locale

    @BeforeTest
    fun setLocale() {
        previousLocale = Locale.getDefault()
        Locale.setDefault(Locale.forLanguageTag("it"))
    }

    @AfterTest
    fun restoreLocale() {
        Locale.setDefault(previousLocale)
    }

    @Test
    fun autoCaptured() = runTest {
        assertEquals(
            "Acquisito automaticamente",
            getString(getSystemResourceEnvironment(), Res.string.problem_report_auto_captured),
        )
    }

    @Test
    fun outputZipFilename() = runTest {
        assertEquals(
            "problem_report.zip",
            getString(getSystemResourceEnvironment(), Res.string.problem_report_output_zip_filename),
        )
    }

    @Test
    fun submitError() = runTest {
        assertEquals(
            "Impossibile creare il report",
            getString(getSystemResourceEnvironment(), Res.string.problem_report_submit_error),
        )
    }

    @Test
    fun problemTypeBug() = runTest {
        assertEquals(
            "Bug / Comportamento imprevisto",
            getString(getSystemResourceEnvironment(), Res.string.problem_type_bug),
        )
    }

    @Test
    fun problemTypeUi() = runTest {
        assertEquals(
            "UI / Problema di visualizzazione",
            getString(getSystemResourceEnvironment(), Res.string.problem_type_ui),
        )
    }

    @Test
    fun problemTypePerformance() = runTest {
        assertEquals(
            "Prestazioni / Crash",
            getString(getSystemResourceEnvironment(), Res.string.problem_type_performance),
        )
    }

    @Test
    fun problemTypeAccount() = runTest {
        assertEquals(
            "Account / Problema di accesso",
            getString(getSystemResourceEnvironment(), Res.string.problem_type_account),
        )
    }

    @Test
    fun problemTypeContent() = runTest {
        assertEquals(
            "Problema di contenuto",
            getString(getSystemResourceEnvironment(), Res.string.problem_type_content),
        )
    }

    @Test
    fun problemTypeOther() = runTest {
        assertEquals("Altro", getString(getSystemResourceEnvironment(), Res.string.problem_type_other))
    }

    @Test
    fun emailSubjectFormat() = runTest {
        assertEquals(
            "Segnalazione problema [abc-123]",
            getString(getSystemResourceEnvironment(), Res.string.problem_report_email_subject_format, "abc-123"),
        )
    }

    @Test
    fun charCounterFormat() = runTest {
        assertEquals(
            "42/500",
            getString(getSystemResourceEnvironment(), Res.string.problem_report_char_counter_format, 42, 500),
        )
    }

    @Test
    fun metadataReportId() = runTest {
        assertEquals("ID report:", getString(getSystemResourceEnvironment(), Res.string.metadata_report_id))
    }

    @Test
    fun metadataProblemType() = runTest {
        assertEquals("Tipo di problema:", getString(getSystemResourceEnvironment(), Res.string.metadata_problem_type))
    }

    @Test
    fun metadataOs() = runTest {
        assertEquals("Sistema operativo:", getString(getSystemResourceEnvironment(), Res.string.metadata_os))
    }

    @Test
    fun metadataDevice() = runTest {
        assertEquals("Dispositivo:", getString(getSystemResourceEnvironment(), Res.string.metadata_device))
    }

    @Test
    fun metadataDescription() = runTest {
        assertEquals("Descrizione:", getString(getSystemResourceEnvironment(), Res.string.metadata_description))
    }

    @Test
    fun metadataStepsToReproduce() = runTest {
        assertEquals(
            "Passi per riprodurre:",
            getString(getSystemResourceEnvironment(), Res.string.metadata_steps_to_reproduce),
        )
    }
}

// ── Turkish ───────────────────────────────────────────────────────────────────

class TurkishStringsTest {

    private lateinit var previousLocale: Locale

    @BeforeTest
    fun setLocale() {
        previousLocale = Locale.getDefault()
        Locale.setDefault(Locale.forLanguageTag("tr"))
    }

    @AfterTest
    fun restoreLocale() {
        Locale.setDefault(previousLocale)
    }

    @Test
    fun autoCaptured() = runTest {
        assertEquals(
            "Otomatik olarak eklendi",
            getString(getSystemResourceEnvironment(), Res.string.problem_report_auto_captured),
        )
    }

    @Test
    fun outputZipFilename() = runTest {
        assertEquals(
            "problem_report.zip",
            getString(getSystemResourceEnvironment(), Res.string.problem_report_output_zip_filename),
        )
    }

    @Test
    fun submitError() = runTest {
        assertEquals(
            "Rapor oluşturulamadı",
            getString(getSystemResourceEnvironment(), Res.string.problem_report_submit_error),
        )
    }

    @Test
    fun problemTypeBug() = runTest {
        assertEquals(
            "Hata / Beklenmedik davranış",
            getString(getSystemResourceEnvironment(), Res.string.problem_type_bug),
        )
    }

    @Test
    fun problemTypeUi() = runTest {
        assertEquals("UI / Görüntüleme sorunu", getString(getSystemResourceEnvironment(), Res.string.problem_type_ui))
    }

    @Test
    fun problemTypePerformance() = runTest {
        assertEquals(
            "Performans / Çökme sorunları",
            getString(getSystemResourceEnvironment(), Res.string.problem_type_performance),
        )
    }

    @Test
    fun problemTypeAccount() = runTest {
        assertEquals("Hesap / Giriş sorunu", getString(getSystemResourceEnvironment(), Res.string.problem_type_account))
    }

    @Test
    fun problemTypeContent() = runTest {
        assertEquals("İçerik sorunu", getString(getSystemResourceEnvironment(), Res.string.problem_type_content))
    }

    @Test
    fun problemTypeOther() = runTest {
        assertEquals("Diğer", getString(getSystemResourceEnvironment(), Res.string.problem_type_other))
    }

    @Test
    fun emailSubjectFormat() = runTest {
        assertEquals(
            "Sorun Raporu [abc-123]",
            getString(getSystemResourceEnvironment(), Res.string.problem_report_email_subject_format, "abc-123"),
        )
    }

    @Test
    fun charCounterFormat() = runTest {
        assertEquals(
            "42/500",
            getString(getSystemResourceEnvironment(), Res.string.problem_report_char_counter_format, 42, 500),
        )
    }

    @Test
    fun metadataReportId() = runTest {
        assertEquals("Rapor Kimliği:", getString(getSystemResourceEnvironment(), Res.string.metadata_report_id))
    }

    @Test
    fun metadataProblemType() = runTest {
        assertEquals("Sorun Türü:", getString(getSystemResourceEnvironment(), Res.string.metadata_problem_type))
    }

    @Test
    fun metadataOs() = runTest {
        assertEquals("İşletim Sistemi:", getString(getSystemResourceEnvironment(), Res.string.metadata_os))
    }

    @Test
    fun metadataDevice() = runTest {
        assertEquals("Cihaz:", getString(getSystemResourceEnvironment(), Res.string.metadata_device))
    }

    @Test
    fun metadataDescription() = runTest {
        assertEquals("Açıklama:", getString(getSystemResourceEnvironment(), Res.string.metadata_description))
    }

    @Test
    fun metadataStepsToReproduce() = runTest {
        assertEquals(
            "Yeniden Üretme Adımları:",
            getString(getSystemResourceEnvironment(), Res.string.metadata_steps_to_reproduce),
        )
    }
}

// ── French ────────────────────────────────────────────────────────────────────

class FrenchStringsTest {

    private lateinit var previousLocale: Locale

    @BeforeTest
    fun setLocale() {
        previousLocale = Locale.getDefault()
        Locale.setDefault(Locale.forLanguageTag("fr"))
    }

    @AfterTest
    fun restoreLocale() {
        Locale.setDefault(previousLocale)
    }

    @Test
    fun autoCaptured() = runTest {
        assertEquals(
            "Capturé automatiquement",
            getString(getSystemResourceEnvironment(), Res.string.problem_report_auto_captured),
        )
    }

    @Test
    fun outputZipFilename() = runTest {
        assertEquals(
            "problem_report.zip",
            getString(getSystemResourceEnvironment(), Res.string.problem_report_output_zip_filename),
        )
    }

    @Test
    fun submitError() = runTest {
        assertEquals(
            "Échec de la création du rapport",
            getString(getSystemResourceEnvironment(), Res.string.problem_report_submit_error),
        )
    }

    @Test
    fun problemTypeBug() = runTest {
        assertEquals(
            "Bug / Comportement inattendu",
            getString(getSystemResourceEnvironment(), Res.string.problem_type_bug),
        )
    }

    @Test
    fun problemTypeUi() = runTest {
        assertEquals("UI / Problème d'affichage", getString(getSystemResourceEnvironment(), Res.string.problem_type_ui))
    }

    @Test
    fun problemTypePerformance() = runTest {
        assertEquals(
            "Performance / Plantages",
            getString(getSystemResourceEnvironment(), Res.string.problem_type_performance),
        )
    }

    @Test
    fun problemTypeAccount() = runTest {
        assertEquals(
            "Compte / Problème de connexion",
            getString(getSystemResourceEnvironment(), Res.string.problem_type_account),
        )
    }

    @Test
    fun problemTypeContent() = runTest {
        assertEquals("Problème de contenu", getString(getSystemResourceEnvironment(), Res.string.problem_type_content))
    }

    @Test
    fun problemTypeOther() = runTest {
        assertEquals("Autre", getString(getSystemResourceEnvironment(), Res.string.problem_type_other))
    }

    @Test
    fun emailSubjectFormat() = runTest {
        assertEquals(
            "Rapport de problème [abc-123]",
            getString(getSystemResourceEnvironment(), Res.string.problem_report_email_subject_format, "abc-123"),
        )
    }

    @Test
    fun charCounterFormat() = runTest {
        assertEquals(
            "42/500",
            getString(getSystemResourceEnvironment(), Res.string.problem_report_char_counter_format, 42, 500),
        )
    }

    @Test
    fun metadataReportId() = runTest {
        assertEquals("ID du rapport :", getString(getSystemResourceEnvironment(), Res.string.metadata_report_id))
    }

    @Test
    fun metadataProblemType() = runTest {
        assertEquals("Type de problème :", getString(getSystemResourceEnvironment(), Res.string.metadata_problem_type))
    }

    @Test
    fun metadataOs() = runTest {
        assertEquals("Système d'exploitation :", getString(getSystemResourceEnvironment(), Res.string.metadata_os))
    }

    @Test
    fun metadataDevice() = runTest {
        assertEquals("Appareil :", getString(getSystemResourceEnvironment(), Res.string.metadata_device))
    }

    @Test
    fun metadataDescription() = runTest {
        assertEquals("Description :", getString(getSystemResourceEnvironment(), Res.string.metadata_description))
    }

    @Test
    fun metadataStepsToReproduce() = runTest {
        assertEquals(
            "Étapes pour reproduire :",
            getString(getSystemResourceEnvironment(), Res.string.metadata_steps_to_reproduce),
        )
    }
}

// ── German ────────────────────────────────────────────────────────────────────

class GermanStringsTest {

    private lateinit var previousLocale: Locale

    @BeforeTest
    fun setLocale() {
        previousLocale = Locale.getDefault()
        Locale.setDefault(Locale.forLanguageTag("de"))
    }

    @AfterTest
    fun restoreLocale() {
        Locale.setDefault(previousLocale)
    }

    @Test
    fun autoCaptured() = runTest {
        assertEquals(
            "Automatisch erfasst",
            getString(getSystemResourceEnvironment(), Res.string.problem_report_auto_captured),
        )
    }

    @Test
    fun outputZipFilename() = runTest {
        assertEquals(
            "problem_report.zip",
            getString(getSystemResourceEnvironment(), Res.string.problem_report_output_zip_filename),
        )
    }

    @Test
    fun submitError() = runTest {
        assertEquals(
            "Bericht konnte nicht erstellt werden",
            getString(getSystemResourceEnvironment(), Res.string.problem_report_submit_error),
        )
    }

    @Test
    fun problemTypeBug() = runTest {
        assertEquals(
            "Fehler / Unerwartetes Verhalten",
            getString(getSystemResourceEnvironment(), Res.string.problem_type_bug),
        )
    }

    @Test
    fun problemTypeUi() = runTest {
        assertEquals("UI / Anzeigeproblem", getString(getSystemResourceEnvironment(), Res.string.problem_type_ui))
    }

    @Test
    fun problemTypePerformance() = runTest {
        assertEquals(
            "Leistung / Abstürze",
            getString(getSystemResourceEnvironment(), Res.string.problem_type_performance),
        )
    }

    @Test
    fun problemTypeAccount() = runTest {
        assertEquals(
            "Konto / Anmeldeproblem",
            getString(getSystemResourceEnvironment(), Res.string.problem_type_account),
        )
    }

    @Test
    fun problemTypeContent() = runTest {
        assertEquals("Inhaltsproblem", getString(getSystemResourceEnvironment(), Res.string.problem_type_content))
    }

    @Test
    fun problemTypeOther() = runTest {
        assertEquals("Sonstiges", getString(getSystemResourceEnvironment(), Res.string.problem_type_other))
    }

    @Test
    fun emailSubjectFormat() = runTest {
        assertEquals(
            "Problembericht [abc-123]",
            getString(getSystemResourceEnvironment(), Res.string.problem_report_email_subject_format, "abc-123"),
        )
    }

    @Test
    fun charCounterFormat() = runTest {
        assertEquals(
            "42/500",
            getString(getSystemResourceEnvironment(), Res.string.problem_report_char_counter_format, 42, 500),
        )
    }

    @Test
    fun metadataReportId() = runTest {
        assertEquals("Berichts-ID:", getString(getSystemResourceEnvironment(), Res.string.metadata_report_id))
    }

    @Test
    fun metadataProblemType() = runTest {
        assertEquals("Problemtyp:", getString(getSystemResourceEnvironment(), Res.string.metadata_problem_type))
    }

    @Test
    fun metadataOs() = runTest {
        assertEquals("Betriebssystem:", getString(getSystemResourceEnvironment(), Res.string.metadata_os))
    }

    @Test
    fun metadataDevice() = runTest {
        assertEquals("Gerät:", getString(getSystemResourceEnvironment(), Res.string.metadata_device))
    }

    @Test
    fun metadataDescription() = runTest {
        assertEquals("Beschreibung:", getString(getSystemResourceEnvironment(), Res.string.metadata_description))
    }

    @Test
    fun metadataStepsToReproduce() = runTest {
        assertEquals(
            "Schritte zur Reproduktion:",
            getString(getSystemResourceEnvironment(), Res.string.metadata_steps_to_reproduce),
        )
    }
}
