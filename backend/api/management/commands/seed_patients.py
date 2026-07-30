import random
from datetime import timedelta, date
from django.core.management.base import BaseCommand
from django.contrib.auth.models import User
from api.models import HealthFacility, UserProfile, Patient, TriageSession

class Command(BaseCommand):
    help = 'Seeds the database with 1000 patients and triage sessions linked to a specific user and facility.'

    def handle(self, *args, **kwargs):
        phone_number = '+254708099875'
        facility_name = 'MTRH ELDORET'
        
        self.stdout.write(self.style.NOTICE(f'Ensuring HealthFacility "{facility_name}" exists...'))
        facility, created = HealthFacility.objects.get_or_create(
            name=facility_name,
            defaults={
                'county': 'Uasin Gishu',
                'country': 'Kenya',
                'is_draft': False
            }
        )
        if created:
            self.stdout.write(self.style.SUCCESS(f'Created facility: {facility_name}'))
        
        self.stdout.write(self.style.NOTICE(f'Ensuring UserProfile with phone "{phone_number}" exists...'))
        
        user_profile, created = UserProfile.objects.get_or_create(
            phone_number=phone_number,
            defaults={
                'full_name': 'Test User',
                'profession': 'Nurse',
                'role': 'CLINICIAN',
                'facility': facility,
                'facility_name': facility.name,
                'county': facility.county,
                'is_approved': True,
                'is_active': True
            }
        )
        if created:
            self.stdout.write(self.style.SUCCESS(f'Created UserProfile for {phone_number}'))
        else:
            user_profile.facility = facility
            user_profile.facility_name = facility.name
            user_profile.save()

        self.stdout.write(self.style.NOTICE('Generating 1000 patients... This might take a moment.'))
        
        first_names = ['Kamau', 'Ochieng', 'Achieng', 'Wanjiru', 'Muthoni', 'Kipchoge', 'Kiprotich', 'Akinyi', 'Mutua', 'Nanjala', 'Ouma', 'Wafula', 'Nekesa', 'Anyango', 'Waweru', 'Kariuki', 'Njoroge', 'Odhiambo', 'Otieno', 'Wamalwa', 'Mwangi', 'Maina']
        last_names = ['Odinga', 'Kenyatta', 'Ruto', 'Ndung\'u', 'Kimani', 'Chebet', 'Koech', 'Mutisya', 'Mwangangi', 'Wanyonyi', 'Oloo', 'Nyongesa', 'Barasa', 'Kipkemboi', 'Karanja', 'Kiprop', 'Nderitu', 'Githae']
        
        patients_to_create = []
        patient_count = 1000
        
        existing_count = Patient.objects.filter(facility=facility).count()
        
        for i in range(patient_count):
            uid = f"MTRH-{(existing_count + i + 1):06d}"
            fname = random.choice(first_names)
            lname = random.choice(last_names)
            
            days_old = random.randint(30, 365 * 15)
            dob = date.today() - timedelta(days=days_old)
            
            p = Patient(
                patient_uid=uid,
                full_name=f"{fname} {lname}",
                date_of_birth=dob,
                sex=random.choice(['Male', 'Female']),
                caregiver_name=f"{random.choice(first_names)} {lname}",
                guardian_phone=f"+2547{random.randint(10000000, 99999999)}",
                facility=facility,
                facility_name=facility.name,
                county=facility.county,
                risk_level=random.choice(['LOW', 'MODERATE', 'HIGH']),
                created_by=user_profile
            )
            patients_to_create.append(p)
            
        created_patients = Patient.objects.bulk_create(patients_to_create)
        self.stdout.write(self.style.SUCCESS(f'Successfully created {len(created_patients)} patients.'))
        
        self.stdout.write(self.style.NOTICE('Generating triage sessions for the patients...'))
        
        triage_sessions = []
        for p in created_patients:
            if random.random() > 0.5:
                temp = round(random.uniform(36.5, 39.5), 1)
                triage_sessions.append(
                    TriageSession(
                        patient=p,
                        visit_number=1,
                        visit_type='FACILITY',
                        temperature=temp,
                        respiratory_rate=random.randint(20, 60),
                        weight_kg=round(random.uniform(3.0, 25.0), 1),
                        muac_mm=random.choice([None, round(random.uniform(110.0, 150.0), 1)]),
                        danger_signs="None" if temp < 38.5 else "Fever",
                        cough_classification="None" if random.random() > 0.3 else "Fast breathing",
                    )
                )
                
        TriageSession.objects.bulk_create(triage_sessions)
        self.stdout.write(self.style.SUCCESS(f'Successfully created {len(triage_sessions)} triage sessions.'))
        
        self.stdout.write(self.style.SUCCESS('Done! Seed complete.'))
