from django.db import models

class UserProfile(models.Model):
    """Health worker profile model (CHW, Nurse, Clinical Officer)."""
    full_name = models.CharField(max_length=255)
    phone_number = models.CharField(max_length=20, unique=True)
    email = models.EmailField(blank=True, null=True)
    profession = models.CharField(max_length=100)
    professional_number = models.CharField(max_length=100, blank=True, null=True)
    facility_name = models.CharField(max_length=255)
    county = models.CharField(max_length=100)
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.full_name} ({self.profession} - {self.facility_name})"


class Patient(models.Model):
    """Pediatric patient profile model."""
    patient_uid = models.CharField(max_length=50, unique=True)
    full_name = models.CharField(max_length=255)
    date_of_birth = models.DateField()
    sex = models.CharField(max_length=10, choices=[('Male', 'Male'), ('Female', 'Female')])
    caregiver_name = models.CharField(max_length=255, blank=True, null=True)
    guardian_phone = models.CharField(max_length=20, blank=True, null=True)
    birth_certificate_number = models.CharField(max_length=100, blank=True, null=True)
    facility_name = models.CharField(max_length=255)
    county = models.CharField(max_length=100)
    risk_level = models.CharField(max_length=20, default='LOW')
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.full_name} ({self.patient_uid})"


class TriageSession(models.Model):
    """WHO IMCI Triage Assessment session model."""
    patient = models.ForeignKey(Patient, on_delete=models.CASCADE, related_with='triage_sessions')
    danger_signs = models.TextField(blank=True, default='')
    temperature = models.FloatField(blank=True, null=True)
    respiratory_rate = models.IntegerField(blank=True, null=True)
    weight_kg = models.FloatField(blank=True, null=True)
    muac_mm = models.FloatField(blank=True, null=True)
    cough_classification = models.CharField(max_length=100, blank=True, null=True)
    diarrhea_classification = models.CharField(max_length=100, blank=True, null=True)
    fever_classification = models.CharField(max_length=100, blank=True, null=True)
    ear_classification = models.CharField(max_length=100, blank=True, null=True)
    nutrition_classification = models.CharField(max_length=100, blank=True, null=True)
    overall_risk = models.CharField(max_length=50, default='LOW')
    treatment_notes = models.TextField(blank=True, default='')
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"Triage for {self.patient.full_name} - {self.overall_risk} ({self.created_at.strftime('%Y-%m-%d')})"


class Vaccination(models.Model):
    """KEPI Immunization log model."""
    patient = models.ForeignKey(Patient, on_delete=models.CASCADE, related_with='vaccinations')
    vaccine_name = models.CharField(max_length=100)
    target_disease = models.CharField(max_length=255)
    dose_number = models.IntegerField(default=1)
    is_given = models.BooleanField(default=False)
    given_date = models.DateField(blank=True, null=True)
    batch_number = models.CharField(max_length=100, blank=True, null=True)

    def __str__(self):
        return f"{self.vaccine_name} for {self.patient.full_name} - Given: {self.is_given}"


class ChatMessage(models.Model):
    """AfyaGPT AI Decision Support conversation history model."""
    patient = models.ForeignKey(Patient, on_delete=models.CASCADE, related_with='chat_messages', blank=True, null=True)
    sender = models.CharField(max_length=20, choices=[('USER', 'USER'), ('AI', 'AI')])
    message_text = models.TextField()
    timestamp = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"[{self.sender}] {self.message_text[:30]}..."
